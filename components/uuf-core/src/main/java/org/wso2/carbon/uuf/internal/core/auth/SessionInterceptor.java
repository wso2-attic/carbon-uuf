package org.wso2.carbon.uuf.internal.core.auth;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.exception.UnauthorizedException;
import org.wso2.carbon.uuf.spi.InterceptorHandler;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

import java.util.HashSet;
import java.util.Set;

@Component(
        name = "org.wso2.carbon.uuf.internal.core.auth.SessionInterceptor",
        service = {Interceptor.class, InterceptorHandler.class},
        immediate = true
)
public class SessionInterceptor implements Interceptor, InterceptorHandler {

    private static final Set<String> urlsToBeSecured = new HashSet<>();

    @Override
    public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo) throws Exception {
        String url = (String) request.getProperties().get("REQUEST_URL");
        if (isValidURL(url)) {
            try {
                return API.validateSession(request, url.substring(0, url.indexOf("/api")));
            } catch (Exception e) {
                throw new UnauthorizedException("You are not permitted to access " + url);
            }
        } else {
            return true;
        }
    }

    @Override
    public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) throws Exception {
        System.out.println("post call");
    }

    private boolean isValidURL(String url) {
        return url.contains("/api") && urlsToBeSecured.contains(url.substring(0, url.indexOf("/api") + 4));
    }

    @Override
    public void addURL(String url) {
        urlsToBeSecured.add(url);
    }

    @Override
    public void removeURL(String url) {
        urlsToBeSecured.remove(url);
    }
}
