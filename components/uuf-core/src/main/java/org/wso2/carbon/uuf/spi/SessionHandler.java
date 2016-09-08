package org.wso2.carbon.uuf.spi;

import org.wso2.carbon.uuf.api.auth.Session;

import java.util.Optional;

public interface SessionHandler {

    void addSession(Session session);

    Optional<Session> getSession(String sessionId);

    boolean removeSession(String sessionId);

    boolean validateSession(String sessionId);
}
