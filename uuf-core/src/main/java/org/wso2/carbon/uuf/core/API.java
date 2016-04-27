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

public class API {

    private final SessionRegistry sessionRegistry;

    public API(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * Returns the result of the method invocation of the best matched OSGi service.
     * @param serviceClassName service class name
     * @param serviceMethodName method name
     * @param args method arguments
     * @return
     */
    public Object callOSGiService(String serviceClassName, String serviceMethodName, Object... args) {
        Object serviceInstance = null;
        try {
            Class<?> serviceClass = Class.forName(serviceClassName);
            Context context = new InitialContext();
            serviceInstance = context.lookup("osgi:service/" + serviceClassName);
            if (serviceInstance == null) {
                throw new IllegalArgumentException("Cannot find any OSGi service registered with the name '" +
                        serviceClassName + "'");
            }
            //If the underlying method is static, then the specified obj argument is ignored.
            return MethodUtils.invokeMethod(serviceInstance, serviceMethodName, args);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot find OSGi service class '" + serviceClassName + "'.");
        } catch (NamingException e) {
            throw new UUFException(
                    "Cannot create the initial context when calling OSGi service '" + serviceClassName
                            + "'.");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Cannot find any method with the signature '" +
                    serviceMethodName + "(" + getParametersTypesStr(args) + ")' on OSGi service '" +
                    serviceInstance.getClass().getName() + "' with service class '" + serviceClassName +
                    "'.");
        } catch (Exception e) {
            throw new UUFException("Invoking method '" + serviceMethodName + "(" +
                    getParametersTypesStr(args) + ")' on OSGi service '" +
                    serviceInstance.getClass().getName() + "' with service class '" + serviceClassName +
                    "' failed.", e);
        }
    }

    /**
     * Returns a map of service implementation class names and instances of all OSGi services for the given
     * service class name.
     * @param serviceClassName
     * @return a map of implementation class and instances
     */
    public Map<String, Object> getOSGiServices(String serviceClassName) {
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

    public void callMicroService() {
        throw new UnsupportedOperationException("To be implemented");
    }

    public void createSession() {
        throw new UnsupportedOperationException("To be implemented");
    }

    public void sendError(int status, String message) {
        throw new HTTPErrorException(status, message);
    }

    public void sendRedirect(String redirectUrl) {
        throw new PageRedirectException(redirectUrl);
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
