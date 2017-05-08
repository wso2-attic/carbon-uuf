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
import org.wso2.carbon.uuf.exception.SessionManagementException;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.auth.SessionManager;
import org.wso2.carbon.uuf.spi.auth.User;

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
 * Manages sessions in memory for a single UUF app.
 * <p>
 * This session manager uses the {@link javax.cache.Cache} for saving the state of the sessions.
 *
 * @since 1.0.0
 */
public class InMemorySessionManager implements SessionManager {

    private static final long SESSION_DEFAULT_TIMEOUT = 1200L; // 20 minutes
    private static final String COOKIE_SESSION_ID = "UUFSESSIONID";
    private static final String COOKIE_CSRF_TOKEN = "CSRFTOKEN";

    private final Cache<String, Session> cache;

    /**
     * Constructs a new InMemorySessionManager.
     *
     * @param appName       name of the UUF application (or app context)
     * @param configuration app configuration
     */
    public InMemorySessionManager(String appName, Configuration configuration) {
        this.cache = createCache(appName, configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session createSession(User user, HttpRequest request, HttpResponse response)
            throws SessionManagementException {
        Session session = new Session(user);
        cache.put(session.getSessionId(), session);

        // Create cookies
        response.addCookie(COOKIE_SESSION_ID, session.getSessionId() +
                "; Path=" + request.getContextPath() + "; Secure; HTTPOnly");
        response.addCookie(COOKIE_CSRF_TOKEN, session.getCsrfToken() + "; Path=" +
                request.getContextPath() + "; Secure");
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Session> getSession(HttpRequest request, HttpResponse response)
            throws SessionManagementException {
        String sessionId = request.getCookieValue(COOKIE_SESSION_ID);
        if (sessionId == null) {
            return Optional.empty();
        }
        if (!Session.isValidSessionId(sessionId)) {
            throw new SessionManagementException("Session ID '" + sessionId + "' is invalid.");
        }
        return Optional.ofNullable(cache.get(sessionId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroySession(HttpRequest request, HttpResponse response)
            throws SessionManagementException {
        String sessionId = request.getCookieValue(COOKIE_SESSION_ID);
        if (sessionId == null) {
            return true; // Session not available
        }
        if (!Session.isValidSessionId(sessionId)) {
            throw new SessionManagementException("Session ID '" + sessionId + "' is invalid.");
        }

        // Clear the session cookie by setting its value to an empty string, Max-Age to zero, & Expires to a past date.
        String expiredCookie = "Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:01 GMT; Path=" + request.getContextPath() +
                "; Secure; HTTPOnly";
        response.addCookie(COOKIE_SESSION_ID, expiredCookie);
        response.addCookie(COOKIE_CSRF_TOKEN, expiredCookie);
        return cache.remove(sessionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return Iterables.size(cache);
    }

    /**
     * Creates a cache to store the sessions of the UUF app.
     * <p>
     * If the session timeout duration is not set in the configuration or if the session timeout duration value is 0
     * then the default session timeout duration of 20 minutes will be used in the cache config.
     *
     * @param cacheName     name of the cache
     * @param configuration app configuration
     * @return carbon cache for the specified cache name
     */
    private Cache<String, Session> createCache(String cacheName, Configuration configuration) {
        // Create cache config
        MutableConfiguration<String, Session> cacheConfig = new MutableConfiguration<>();
        cacheConfig.setTypes(String.class, Session.class);
        cacheConfig.setStoreByValue(false);
        long sessionTimeout = configuration.getSessionTimeout();
        sessionTimeout = sessionTimeout == 0 ? SESSION_DEFAULT_TIMEOUT : sessionTimeout;
        Duration sessionTimeOut = new Duration(TimeUnit.SECONDS, sessionTimeout);
        cacheConfig.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(sessionTimeOut));

        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        try {
            Cache<String, Session> cache = cacheManager.getCache(cacheName, String.class, Session.class);
            if (cache == null) {
                cache = cacheManager.createCache(cacheName, cacheConfig);
            }
            return cache;
        } catch (IllegalStateException e) {
            throw new SessionManagementException("Cannot create cache '" + cacheName + "' for session management. " +
                    "Cache manager is closed.", e);
        } catch (CacheException e) {
            throw new SessionManagementException("Cannot create cache '" + cacheName + "' for session management.", e);
        } catch (IllegalArgumentException e) {
            throw new SessionManagementException("Cannot create cache '" + cacheName +
                    "' for session management. Invalid cache configuration.", e);
        } catch (UnsupportedOperationException e) {
            throw new SessionManagementException("Cannot create cache '" + cacheName +
                    "' for session management. Cache configuration specifies an unsupported feature.", e);
        }
    }
}
