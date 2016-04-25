package org.wso2.carbon.uuf.core.auth;

import org.wso2.carbon.uuf.core.exception.UUFException;

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
        cacheConfig.setStoreByValue(true);
        // TODO: read session expire time from configurations
        cacheConfig.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.TWENTY_MINUTES));
        String cacheName = this.getClass().getName() + "-" + appName + "-sessions_cache";
        cache = getCache(cacheName, cacheConfig);
    }

    public void addSession(Session session) {
        cache.put(session.getSessionId(), session);
    }

    public Optional<Session> getSession(String token) {
        return Optional.ofNullable(cache.get(token));
    }

    public boolean removeSession(String token) {
        return cache.remove(token);
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
