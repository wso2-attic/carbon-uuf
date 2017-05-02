/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.carbon.uuf.exception.SessionManagerException;
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
 * Manages sessions in memory.
 * <p>
 * This session manager uses the {@link javax.cache.Cache} for saving the state of the sessions.
 * </p>
 *
 * @since 1.0.0
 */
public class InMemorySessionManager implements SessionManager {

    private static final Object LOCK = new Object();
    private static final String CONFIGURATION_SESSION_TIMEOUT_DURATION = "sessionTimeoutDuration";
    private static final int DEFAULT_SESSION_TIMEOUT_DURATION = 20 * 60;
    private static final String COOKIE_SESSION_NAME = "UUFSESSIONID";
    private static final String COOKIE_CSRF_TOKEN = "CSRFTOKEN";

    /**
     * {@inheritDoc}
     */
    @Override
    public Session createSession(User user, HttpRequest request, HttpResponse response, Configuration configuration)
            throws SessionManagerException {
        String contextPath = request.getContextPath();
        Cache<String, Session> cache = getCache(contextPath, configuration);
        Session session = new Session(user);
        cache.put(session.getSessionId(), session);

        // Create cookies
        response.addCookie(COOKIE_SESSION_NAME, session.getSessionId() +
                "; Path=" + request.getContextPath() + "; Secure; HTTPOnly");
        response.addCookie(COOKIE_CSRF_TOKEN, session.getCsrfToken() + "; Path=" +
                request.getContextPath() + "; Secure");
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Session> getSession(HttpRequest request, HttpResponse response, Configuration configuration)
            throws SessionManagerException {
        String sessionId = request.getCookieValue(COOKIE_SESSION_NAME);
        if (sessionId == null) {
            return Optional.empty();
        }
        if (!Session.isValidSessionId(sessionId)) {
            throw new IllegalArgumentException("Session ID '" + sessionId + "' is invalid.");
        }
        String contextPath = request.getContextPath();
        Cache<String, Session> cache = getCache(contextPath, configuration);
        return Optional.ofNullable(cache.get(sessionId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroySession(HttpRequest request, HttpResponse response, Configuration configuration)
            throws SessionManagerException {
        String sessionId = request.getCookieValue(COOKIE_SESSION_NAME);
        if (sessionId == null) {
            return true; // Session not available
        }
        if (!Session.isValidSessionId(sessionId)) {
            throw new IllegalArgumentException("Session ID '" + sessionId + "' is invalid.");
        }
        String contextPath = request.getContextPath();
        Cache<String, Session> cache = getCache(contextPath, configuration);

        // Clear the session cookie by setting its value to an empty string, Max-Age to zero, & Expires to a past date.
        String expiredCookie = "Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:01 GMT; Path=" + request.getContextPath() +
                "; Secure; HTTPOnly";
        response.addCookie(COOKIE_SESSION_NAME, expiredCookie);
        response.addCookie(COOKIE_CSRF_TOKEN, expiredCookie);
        return cache.remove(sessionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        int size = 0;
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        for (String cacheName : cacheManager.getCacheNames()) {
            size += Iterables.size(cacheManager.getCache(cacheName));
        }
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
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

    /**
     * Returns cache for the specified cache name.
     *
     * @param cacheName     name of the cache
     * @param configuration app configuration
     * @return carbon cache for the specified cache name
     */
    private Cache<String, Session> getCache(String cacheName, Configuration configuration) {
        // Create cache config
        MutableConfiguration<String, Session> cacheConfig = new MutableConfiguration<>();
        cacheConfig.setTypes(String.class, Session.class);
        cacheConfig.setStoreByValue(false);
        Duration sessionTimeOut = getSessionTimeOutDuration(configuration);
        cacheConfig.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(sessionTimeOut));

        // Get cache manager
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();

        try {
            synchronized (LOCK) {
                Cache<String, Session> cache = cacheManager.getCache(cacheName, String.class, Session.class);
                if (cache == null) {
                    cache = cacheManager.createCache(cacheName, cacheConfig);
                }
                return cache;
            }
        } catch (IllegalStateException e) {
            throw new UUFException("Cannot create cache '" + cacheName + "' for session management. Cache manager '" +
                    cacheManager.getURI() + "' is closed.", e);
        } catch (CacheException e) {
            throw new UUFException("Cannot create cache '" + cacheName + "' for session management.", e);
        } catch (IllegalArgumentException e) {
            throw new UUFException("Cannot create cache '" + cacheName +
                    "' for session management. Invalid cache configuration.", e);
        } catch (UnsupportedOperationException e) {
            throw new UUFException("Cannot create cache '" + cacheName +
                    "' for session management. Cache configuration specifies an unsupported feature.", e);
        }
    }

    /**
     * Returns the session time-out duration from the configuration.
     *
     * @param configuration app configuration
     * @return session time-out duration
     * @throws SessionManagerException if session timeout duration in configuration is not an integer value
     */
    private Duration getSessionTimeOutDuration(Configuration configuration) throws SessionManagerException {
        Map<String, Object> otherConfigurations = configuration.other();
        int sessionTimeoutDuration = DEFAULT_SESSION_TIMEOUT_DURATION;
        if (otherConfigurations != null) {
            try {
                sessionTimeoutDuration = (int) otherConfigurations.getOrDefault(CONFIGURATION_SESSION_TIMEOUT_DURATION,
                        DEFAULT_SESSION_TIMEOUT_DURATION);
            } catch (ClassCastException e) {
                throw new SessionManagerException(CONFIGURATION_SESSION_TIMEOUT_DURATION + " in app.yaml " +
                        "configuration should be an integer value", e);
            }
        }
        return new Duration(TimeUnit.SECONDS, sessionTimeoutDuration);
    }
}
