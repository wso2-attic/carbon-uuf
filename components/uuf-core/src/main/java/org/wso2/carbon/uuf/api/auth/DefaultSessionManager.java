/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.api.auth;

import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.auth.SessionRegistry;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.auth.SessionManager;
import org.wso2.carbon.uuf.spi.auth.User;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DefaultSessionManager implements SessionManager {
    MutableConfiguration<String, Session> cacheConfig;

    public DefaultSessionManager() {
        this(20 * 60);
    }

    public DefaultSessionManager(int sessionTimeoutDuration) {
        cacheConfig = new MutableConfiguration<>();
        cacheConfig.setTypes(String.class, Session.class);
        cacheConfig.setStoreByValue(false);
        Duration sessionTimeout = new Duration(TimeUnit.SECONDS, sessionTimeoutDuration);
        cacheConfig.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(sessionTimeout));
    }

    private static final Object LOCK = new Object();

    private static Cache<String, Session> getCache(String cacheName,
                                                   MutableConfiguration<String, Session> cacheConfig) {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        Cache<String, Session> cache;
        try {
            synchronized (LOCK) {
                cache = cacheManager.getCache(cacheName, String.class, Session.class);
                if (cache == null) {
                    cache = cacheManager.createCache(cacheName, cacheConfig);
                }
            }
            return cache;
        } catch (IllegalStateException e) {
            throw new UUFException("Cannot create cache '" + cacheName + "' for session management. Cache manager '" +
                                           cacheManager.getURI() + "' is closed.", e);
        } catch (CacheException e) {
            throw new UUFException("Cannot create cache '" + cacheName + "' for session management.", e);
        } catch (IllegalArgumentException e) {
            throw new UUFException("Cannot create cache '" + cacheName +
                                           "' for session management. Invalid cache configuration.", e);
        } catch (UnsupportedOperationException e) {
            throw new UUFException(
                    "Cannot create cache '" + cacheName +
                            "' for session management. Cache configuration specifies an unsupported feature.", e);
        }
    }

    @Override
    public Session createSession(String identifier, User user, HttpRequest request, HttpResponse response) {
        String cacheName = getCacheName(identifier);
        Cache<String, Session> cache = getCache(cacheName, cacheConfig);
        Session session = new Session(user);
        cache.put(session.getSessionId(), session);
        return session;
    }

    @Override
    public Optional<Session> getSession(String identifier, HttpRequest request, HttpResponse response) {
        String cacheName = getCacheName(identifier);
        Cache<String, Session> cache = getCache(cacheName, cacheConfig);
        String sessionId = request.getCookieValue(SessionRegistry.SESSION_COOKIE_NAME);
        if (!Session.isValidSessionId(sessionId)) {
            throw new IllegalArgumentException("Session ID '" + sessionId + "' is invalid.");
        }
        return Optional.ofNullable(cache.get(sessionId));
    }

    @Override
    public boolean removeSession(String identifier, HttpRequest request, HttpResponse response) {
        String cacheName = getCacheName(identifier);
        Cache<String, Session> cache = getCache(cacheName, cacheConfig);
        String sessionId = request.getCookieValue(SessionRegistry.SESSION_COOKIE_NAME);
        if (!Session.isValidSessionId(sessionId)) {
            throw new IllegalArgumentException("Session ID '" + sessionId + "' is invalid.");
        }
        return cache.remove(sessionId);
    }

    private String getCacheName(String identifier) {
        return this.getClass().getName() + "-" + identifier + "-sessions_cache";
    }

    /**
     * Closes this stream and releases any system resources associated with it. If the stream is already closed then
     * invoking this method has no effect.
     * <p>
     * <p> As noted in {@link AutoCloseable#close()}, cases where the close may fail require careful attention. It is
     * strongly advised to relinquish the underlying resources and to internally <em>mark</em> the {@code Closeable} as
     * closed, prior to throwing the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        cacheManager.close();
    }
}
