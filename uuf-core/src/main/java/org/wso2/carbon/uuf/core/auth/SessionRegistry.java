package org.wso2.carbon.uuf.core.auth;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

public class SessionRegistry implements Closeable {
    private Cache<String, Session> cache;

    public SessionRegistry() {
        MutableConfiguration<String, Session> config = new MutableConfiguration<>();
        config.setTypes(String.class, Session.class);
        config.setStoreByValue(true);
        // TODO: read session expire time from a config
        config.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.TWENTY_MINUTES));
        String cacheName = this.getClass().getName() + "-sessions_cache";
        cache = Caching.getCachingProvider().getCacheManager().createCache(cacheName, config);
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
            cache.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
