/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.uuf.internal.core.auth;

import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.exception.UUFException;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

public class SessionRegistry implements Closeable {

    public static final String SESSION_COOKIE_NAME = "UUFSESSIONID";
    private static final Object LOCK = new Object();

    private final Cache<String, Session> cache;

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

    public SessionRegistry(String appName) {
        MutableConfiguration<String, Session> cacheConfig = new MutableConfiguration<>();
        cacheConfig.setTypes(String.class, Session.class);
        cacheConfig.setStoreByValue(false);
        // TODO: read session expire time from configurations
        cacheConfig.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.TWENTY_MINUTES));
        String cacheName = this.getClass().getName() + "-" + appName + "-sessions_cache";
        cache = getCache(cacheName, cacheConfig);
    }

    public void addSession(Session session) {
        cache.put(session.getSessionId(), session);
    }

    public Optional<Session> getSession(String sessionId) {
        if (!Session.isValidSessionId(sessionId)) {
            throw new IllegalArgumentException("Session ID '" + sessionId + "' is invalid.");
        }
        return Optional.ofNullable(cache.get(sessionId));
    }

    public boolean removeSession(String sessionId) {
        if (!Session.isValidSessionId(sessionId)) {
            throw new IllegalArgumentException("Session ID '" + sessionId + "' is invalid.");
        }
        return cache.remove(sessionId);
    }

    public void removeAllSessions() {
        cache.clear();
    }

    @Override
    public void close() throws IOException {
        if (!cache.isClosed()) {
            cache.clear();
            cache.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
