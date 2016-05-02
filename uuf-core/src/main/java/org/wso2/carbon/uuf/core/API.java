/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.uuf.core;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.wso2.carbon.uuf.core.auth.SessionRegistry;
import org.wso2.carbon.uuf.core.exception.HTTPErrorException;
import org.wso2.carbon.uuf.core.exception.PageRedirectException;
import org.wso2.carbon.uuf.core.exception.UUFException;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;

// TODO remove this SuppressWarnings
@SuppressWarnings("PackageAccessibility")
public class API {

    private final SessionRegistry sessionRegistry;

    public API(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * Returns the result of the method invocation of the best matched OSGi service.
     *
     * @param serviceClassName  service class name
     * @param serviceMethodName method name
     * @param args              method arguments
     * @return
     */
    public static Object callOSGiService(String serviceClassName, String serviceMethodName, Object... args) {
        Object serviceInstance;
        try {
            serviceInstance = (new InitialContext()).lookup("osgi:service/" + serviceClassName);
            if (serviceInstance == null) {
                throw new IllegalArgumentException(
                        "Cannot find any OSGi service registered with the name '" + serviceClassName + "'.");
            }
        } catch (NamingException e) {
            throw new UUFException(
                    "Cannot create the initial context when calling OSGi service '" + serviceClassName + "'.");
        }

        try {
            //If the underlying method is static, then the specified 'object' argument is ignored.
            return MethodUtils.invokeMethod(serviceInstance, serviceMethodName, args);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Cannot find any method with the signature '" + serviceMethodName + "(" +
                            getParametersTypesStr(args) + ")' on OSGi service '" +
                            serviceInstance.getClass().getName() + "' with service class '" + serviceClassName + "'.");
        } catch (Exception e) {
            throw new UUFException(
                    "Invoking method '" + serviceMethodName + "(" + getParametersTypesStr(args) +
                            ")' on OSGi service '" + serviceInstance.getClass().getName() + "' with service class '" +
                            serviceClassName + "' failed.", e);
        }
    }

    /**
     * Returns a map of service implementation class names and instances of all OSGi services for the given service
     * class name.
     *
     * @param serviceClassName
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
        throw new UnsupportedOperationException("To be implemented");
    }

    public static void sendError(int status, String message) {
        throw new HTTPErrorException(status, message);
    }

    public static void sendRedirect(String redirectUrl) {
        throw new PageRedirectException(redirectUrl);
    }

    public void createSession() {
        throw new UnsupportedOperationException("To be implemented");
    }

    public void setTheme(String name) {
        throw new UnsupportedOperationException("To be implemented");
    }

    private static String getParametersTypesStr(Object[] args) {
        if (args == null) {
            return "";
        }
        StringBuilder signature = new StringBuilder();
        for (Object arg : args) {
            signature.append(arg.getClass().getName());
        }
        return signature.toString();
    }
}
