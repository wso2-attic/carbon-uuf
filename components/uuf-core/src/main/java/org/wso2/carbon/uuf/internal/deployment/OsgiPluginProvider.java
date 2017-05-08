/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.internal.deployment;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.uuf.internal.exception.PluginLoadingException;

import java.util.HashMap;
import java.util.Map;

/**
 * Plugins provider for plugins that are OSGi services.
 *
 * @since 1.0.0
 */
public class OsgiPluginProvider implements PluginProvider {

    private final BundleContext bundleContext;
    private final Map<Class, ServiceTracker> serviceTrackers;

    public OsgiPluginProvider(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.serviceTrackers = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getPluginInstance(Class<T> type, String className, ClassLoader classLoader)
            throws PluginLoadingException {
        T plugin = loadOsgiPlugin(type, className);
        return (plugin != null) ? plugin : loadNonOsgiPlugin(type, className, classLoader);
    }

    /**
     * Creates an instance of the specified class type in OSGi mode.
     * This method will throw {@link PluginLoadingException} when the relevant bundle is not found or on any other
     * exception
     *
     * @param type      type of the instance to be created
     * @param className name of the class
     * @param <T>       type of the instance to be created
     * @return instance of the specified class type
     */
    private <T> T loadOsgiPlugin(Class<T> type, String className) {
        if (bundleContext == null) {
            return null;
        }

        ServiceTracker<T, T> serviceTracker = getServiceTracker(type);
        @SuppressWarnings("unchecked")
        T[] services = serviceTracker.getServices((T[]) new Object[serviceTracker.size()]);
        for (T service : services) {
            if ((service != null) && (service.getClass().getName().equals(className))) {
                return service;
            }
        }

        return null;
    }

    /**
     * Creates an instance of the specified class type in OSGi mode.
     *
     * @param type        type of the instance to be created
     * @param className   name of the class
     * @param classLoader class loader to be used to load the plugin class
     * @param <T>         type of the instance to be created
     * @return instance of the specified class type
     */
    private <T> T loadNonOsgiPlugin(Class<T> type, String className, ClassLoader classLoader) {
        Object pluginInstance;
        try {
            pluginInstance = classLoader.loadClass(className).newInstance();
        } catch (ClassNotFoundException e) {
            throw new PluginLoadingException(
                    "Cannot load plugin '" + className + "' via the class loader '" + classLoader + "'.", e);

        } catch (IllegalAccessException | InstantiationException | SecurityException e) {
            throw new PluginLoadingException(
                    "Cannot instantiation plugin '" + className + "' via the class loader '" + classLoader + "'.", e);
        }

        try {
            return type.cast(pluginInstance);
        } catch (ClassCastException e) {
            throw new PluginLoadingException(
                    "Plugin '" + className + "' is not a sub class of the plugin type '" + type.getName() + "'.", e);
        }
    }

    private <T> ServiceTracker<T, T> getServiceTracker(Class<T> serviceClass) {
        @SuppressWarnings("unchecked")
        ServiceTracker<T, T> serviceTracker = serviceTrackers.get(serviceClass);
        if (serviceTracker == null) {
            serviceTracker = new ServiceTracker<>(bundleContext, serviceClass, null);
            serviceTracker.open();
            serviceTrackers.put(serviceClass, serviceTracker);
        }
        return serviceTracker;
    }
}
