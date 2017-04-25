/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.api.auth;

import com.google.common.collect.Iterables;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.auth.SessionManager;
import org.wso2.carbon.uuf.spi.auth.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;

/**
 * Manage session instances.
 */
public class InMemorySessionManager implements SessionManager {

    private static final Object LOCK = new Object();
    private static final String SESSION_TIME_OUT = "sessionTimeoutDuration";
    private static final int DEFAULT_SESSION_TIMEOUT_DURATION = 20 * 60;

    private MutableConfiguration<String, Session> cacheConfiguration;
    private CacheManager cacheManager;

    @Override
    public void init(Configuration configuration) {
        MutableConfiguration<String, Session> cacheConfig = new MutableConfiguration<>();
        cacheConfig.setTypes(String.class, Session.class);
        cacheConfig.setStoreByValue(false);

        Map<String, Object> otherConfigurations = configuration.other();
        int sessionTimeoutDuration = DEFAULT_SESSION_TIMEOUT_DURATION;
        if (otherConfigurations != null) {
            sessionTimeoutDuration = (int) otherConfigurations.getOrDefault(SESSION_TIME_OUT,
                    DEFAULT_SESSION_TIMEOUT_DURATION);
        }
        Duration sessionTimeout = new Duration(TimeUnit.SECONDS, sessionTimeoutDuration);
        cacheConfig.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(sessionTimeout));
        this.cacheConfiguration = cacheConfig;
        this.cacheManager = Caching.getCachingProvider().getCacheManager();
    }

    @Override
    public Session createSession(User user, HttpRequest request, HttpResponse response) {
        String contextPath = request.getContextPath();
        Cache<String, Session> cache = getCache(contextPath, cacheConfiguration);
        Session session = new Session(user);
        cache.put(session.getSessionId(), session);
        return session;
    }

    @Override
    public Optional<Session> getSession(HttpRequest request, HttpResponse response) {
        String sessionId = request.getCookieValue(Session.SESSION_COOKIE_NAME);
        if (sessionId == null) {
            return Optional.empty();
        }
        if (!Session.isValidSessionId(sessionId)) {
            throw new IllegalArgumentException("Session ID '" + sessionId + "' is invalid.");
        }
        String contextPath = request.getContextPath();
        Cache<String, Session> cache = getCache(contextPath, cacheConfiguration);
        return Optional.ofNullable(cache.get(sessionId));
    }

    @Override
    public boolean destroySession(HttpRequest request, HttpResponse response) {
        String sessionId = request.getCookieValue(Session.SESSION_COOKIE_NAME);
        if (sessionId == null) {
            return true; // Session not available
        }
        if (!Session.isValidSessionId(sessionId)) {
            throw new IllegalArgumentException("Session ID '" + sessionId + "' is invalid.");
        }
        String contextPath = request.getContextPath();
        Cache<String, Session> cache = getCache(contextPath, cacheConfiguration);
        return cache.remove(sessionId);
    }

    @Override
    public void clear() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache<String, Session> cache = cacheManager.getCache(cacheName);
            if (!cache.isClosed()) {
                cache.clear();
            }
        });
    }

    @Override
    public int getCount() {
        int size = 0;
        for (String cacheName : cacheManager.getCacheNames()) {
            size += Iterables.size(cacheManager.getCache(cacheName));
        }
        return size;
    }

    @Override
    public void close() {
        if (!cacheManager.isClosed()) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                Cache<String, Session> cache = cacheManager.getCache(cacheName);
                if (!cache.isClosed()) {
                    cache.clear();
                    cache.close();
                }
            });
        }
        cacheManager.close();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    private Cache<String, Session> getCache(String cacheName, MutableConfiguration<String, Session> cacheConfig) {
        try {
            synchronized (LOCK) {
                Cache<String, Session> cache = cacheManager.getCache(cacheName, String.class, Session.class);
                if (cache == null) {
                    cache = cacheManager.createCache(cacheName, cacheConfig);
                }
                return cache;
            }
        } catch (IllegalStateException e) {
            throw new UUFException("Cannot create cache '" + cacheName + "' for session management. Cache manager " +
                    "'" + cacheManager.getURI() + "' is closed.", e);
        } catch (CacheException e) {
            throw new UUFException("Cannot create cache '" + cacheName + "' for session management.", e);
        } catch (IllegalArgumentException e) {
            throw new UUFException("Cannot create cache '" + cacheName + "' for session management. Invalid " +
                    "cache configuration.", e);
        } catch (UnsupportedOperationException e) {
            throw new UUFException("Cannot create cache '" + cacheName + "' for session management. Cache " +
                    "configuration specifies an unsupported feature.", e);
        }
    }
}
