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

package org.wso2.carbon.uuf.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.exception.HttpErrorException;
import org.wso2.carbon.uuf.exception.PageRedirectException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.auth.SessionRegistry;
import org.wso2.carbon.uuf.spi.auth.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

// TODO remove this SuppressWarnings
@SuppressWarnings("PackageAccessibility")
public class API {

    private final SessionRegistry sessionRegistry;
    private final RequestLookup requestLookup;
    private Optional<Session> currentSession;

    API(SessionRegistry sessionRegistry, RequestLookup requestLookup) {
        this.sessionRegistry = sessionRegistry;
        this.requestLookup = requestLookup;
        this.currentSession = Optional.<Session>empty();
    }

    /**
     * Returns the request lookup.
     *
     * @return RequestLookup
     */
    public RequestLookup getRequestLookup() {
        return requestLookup;
    }

    /**
     * Returns the result of the method invocation of the best matched OSGi service.
     *
     * @param serviceClassName  service class name
     * @param serviceMethodName method name
     * @param args              method arguments
     * @return invoked OSGi service instance
     */
    public static Object callOSGiService(String serviceClassName, String serviceMethodName, Object... args) {
        Object serviceInstance;
        InitialContext initialContext;
        try {
            initialContext = new InitialContext();
        } catch (NamingException e) {
            throw new UUFException(
                    "Cannot create the JNDI initial context when calling OSGi service '" + serviceClassName + "'.");
        }

        try {
            serviceInstance = initialContext.lookup("osgi:service/" + serviceClassName);
        } catch (NamingException e) {
            throw new UUFException(
                    "Cannot find any OSGi service registered with the name '" + serviceClassName + "'.");
        }

        try {
            return MethodUtils.invokeMethod(serviceInstance, serviceMethodName, args);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Cannot find any method with the signature '" + serviceMethodName + "(" + joinClassNames(args) +
                            ")' in OSGi service '" + serviceInstance.getClass().getName() + "' with service class '" +
                            serviceClassName + "'.");
        } catch (Exception e) {
            throw new UUFException(
                    "Invoking method '" + serviceMethodName + "(" + joinClassNames(args) + ")' on OSGi service '" +
                            serviceInstance.getClass().getName() + "' with service class '" + serviceClassName +
                            "' failed.", e);
        }
    }

    /**
     * Returns a map of service implementation class names and instances of all OSGi services for the given service
     * class name.
     *
     * @param serviceClassName service class name
     * @return a map of implementation class and instances
     */
    public static Map<String, Object> getOSGiServices(String serviceClassName) {
        try {
            Context context = new InitialContext();
            NamingEnumeration<Binding> enumeration = context.listBindings("osgi:service/" + serviceClassName);
            Map<String, Object> services = new HashMap<>();
            while (enumeration.hasMore()) {
                Binding binding = enumeration.next();
                services.put(binding.getClassName(), binding.getObject());
            }
            return services;
        } catch (NamingException e) {
            throw new UUFException("Cannot create the initial context when calling OSGi service '" +
                                           serviceClassName + "'.");
        }
    }

    public static void callMicroService() {
        // TODO: 5/16/16 Call a Microservice through OSGi or network-call accrodingly
        throw new UnsupportedOperationException("To be implemented");
    }

    public static void sendError(int status, String message) {
        if ((status < 100) || (status > 599)) {
            throw new IllegalArgumentException("HTTP status code must be between 100 (inclusive) and 600 (exclusive).");
        }
        if ((message == null) || message.isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty.");
        }
        throw new HttpErrorException(status, message);
    }

    public static void sendRedirect(String redirectUrl) {
        if ((redirectUrl == null) || redirectUrl.isEmpty()) {
            throw new IllegalArgumentException("Redirect URL cannot be null or empty");
        }
        throw new PageRedirectException(redirectUrl);
    }

    /**
     * Creates a new session and returns it.
     *
     * @param user user to create the session
     * @return newly created session
     */
    public Session createSession(User user) {
        destroySession();
        Session session = new Session(user);
        sessionRegistry.addSession(session);
        String header = SessionRegistry.SESSION_COOKIE_NAME + "=" + session.getSessionId() + "; Path=" +
                requestLookup.getContextPath() + "; Secure; HTTPOnly";
        requestLookup.getResponse().setHeader("Set-Cookie", header);
        return session;
    }

    public Optional<Session> getSession() {
        if (!currentSession.isPresent()) {
            // Since an API object lives in the request scope, it is safe to cache the current Session object.
            String sessionId = requestLookup.getRequest().getCookieValue(SessionRegistry.SESSION_COOKIE_NAME);
            if (!StringUtils.isEmpty(sessionId)) {
                currentSession = sessionRegistry.getSession(sessionId);
            }
        }
        return currentSession;
    }

    public boolean destroySession() {
        Optional<Session> session = getSession();
        if (!session.isPresent()) {
            // No session found in the current request.
            return false;
        }

        // Remove session from the SessionRegistry.
        sessionRegistry.removeSession(session.get().getSessionId());
        // Clear the session cookie by setting its value to an empty string, Max-Age to zero, & Expires to a past date.
        String header = SessionRegistry.SESSION_COOKIE_NAME +
                "=; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:01 GMT; Path=" + requestLookup.getContextPath() +
                "; Secure; HTTPOnly";
        requestLookup.getResponse().setHeader("Set-Cookie", header);
        return true;
    }

    private static String joinClassNames(Object[] args) {
        if (args == null) {
            return "null";
        }
        if (args.length == 0) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        for (Object arg : args) {
            buffer.append(arg.getClass().getName()).append(',');
        }
        return buffer.deleteCharAt(buffer.length() - 1).toString();
    }
}
