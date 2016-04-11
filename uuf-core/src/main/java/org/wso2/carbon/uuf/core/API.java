package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.core.auth.SessionRegistry;

public class API {
    private final SessionRegistry sessionRegistry;

    public API(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void callOSGiService() {
        throw new UnsupportedOperationException("To be implemented");
    }

    public void callMicroService() {
        throw new UnsupportedOperationException("To be implemented");
    }

    public void createSession() {
        throw new UnsupportedOperationException("To be implemented");
    }

    public void sendError(int status, String message) {
        throw new UnsupportedOperationException("To be implemented");
    }

    public void sendRedirect(String url) {
        throw new UnsupportedOperationException("To be implemented");
    }

    public void setTheme(String name) {
        throw new UnsupportedOperationException("To be implemented");
    }
}
