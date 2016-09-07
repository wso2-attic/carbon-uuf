package org.wso2.carbon.uuf.internal.core.auth;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

public class SessionHandler implements Interceptor {


    private SessionRegistry sessionRegistry;

    @Override
    public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo) throws Exception {
        String cookie = request.getHeader("Cookie");
        String first = cookie.substring(cookie.indexOf("UUFSESSIONID"));
        String uufSessionId = first.substring(13, first.indexOf(";"));
        Session session = this.sessionRegistry.getSession(uufSessionId).orElse(null);
        String userName = session.getUser().getUsername();
        return false;
    }

    @Override
    public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) throws Exception {

    }

    @Reference(name = "sessionRegistry",
               service = SessionRegistry.class,
               cardinality = ReferenceCardinality.MANDATORY,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetSessionRegistry")
    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void unsetSessionRegistry() {
        this.sessionRegistry = null;
    }
}
