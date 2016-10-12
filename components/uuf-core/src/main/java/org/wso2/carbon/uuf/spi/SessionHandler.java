package org.wso2.carbon.uuf.spi;

import org.wso2.carbon.uuf.api.auth.Session;

import java.util.Optional;

public interface SessionHandler {

    void addSession(Session session, String contextPath);

    Optional<Session> getSession(String sessionId, String contextPath);

    boolean removeSession(String sessionId, String contextPath);

    boolean validateSession(String sessionId, String contextPath);

    String getUserName(String sessionId, String contextPath);

    void createCacheEntry(String appName, String contextPath);
}
