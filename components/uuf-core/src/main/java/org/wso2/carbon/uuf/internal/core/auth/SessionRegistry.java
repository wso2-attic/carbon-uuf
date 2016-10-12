/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.uuf.internal.core.auth;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.spi.SessionHandler;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component(name = "org.wso2.carbon.uuf.internal.core.auth.SessionRegistry",
           service = {SessionHandler.class},
           immediate = true)
@SuppressWarnings("unused")
public class SessionRegistry implements Closeable, SessionHandler {

    public static final String SESSION_COOKIE_NAME = "UUFSESSIONID";
    private static final Logger log = LoggerFactory.getLogger(SessionRegistry.class);
    private static final Object LOCK = new Object();
    private static final Map<String, Cache<String, Session>> sessionRegistry = new HashMap<>();

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
    public void createCacheEntry(String appName, String contextPath) {
        if (sessionRegistry.get(contextPath) != null) {
            return;
        }
        Cache<String, Session> cache;
        MutableConfiguration<String, Session> cacheConfig = new MutableConfiguration<>();
        cacheConfig.setTypes(String.class, Session.class);
        cacheConfig.setStoreByValue(false);
        // TODO: read session expire time from configurations
        cacheConfig.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.TWENTY_MINUTES));
        String cacheName = this.getClass().getName() + "-" + appName + "-sessions_cache";
        cache = getCache(cacheName, cacheConfig);
        sessionRegistry.put(contextPath, cache);
    }

    @Override
    public void addSession(Session session, String contextPath) {
        sessionRegistry.get(contextPath).put(session.getSessionId(), session);
    }

    @Override
    public Optional<Session> getSession(String sessionId, String contextPath) {
        if (!Session.isValidSessionId(sessionId)) {
            throw new IllegalArgumentException("Session ID '" + sessionId + "' is invalid.");
        }
        return Optional.ofNullable(sessionRegistry.get(contextPath).get(sessionId));
    }

    @Override
    public boolean removeSession(String sessionId, String contextPath) {
        if (!Session.isValidSessionId(sessionId)) {
            throw new IllegalArgumentException("Session ID '" + sessionId + "' is invalid.");
        }
        return sessionRegistry.get(contextPath).remove(sessionId);
    }

    @Override
    public boolean validateSession(String sessionId, String contextPath) {
        Session session = this.getSession(sessionId, contextPath).orElse(null);
        if (sessionId.equals(session.getSessionId())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getUserName(String sessionId, String contextPath) {
        Session session = this.getSession(sessionId, contextPath).orElse(null);
        return session.getUser().getUsername();
    }

    public void removeAllSessions(String contextPath) {
        sessionRegistry.get(contextPath).clear();
    }

    @Override
    public void close() throws IOException {
        sessionRegistry.forEach((contextPath, cache) -> {
            if (!cache.isClosed()) {
                cache.clear();
                cache.close();
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        log.debug("SessionRegistry activated.");
    }

    @Deactivate
    protected void deactivate() {
        log.debug("SessionRegistry deactivated.");
    }
}
