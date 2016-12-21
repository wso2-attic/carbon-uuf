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

package org.wso2.carbon.uuf.internal;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import java.util.function.Consumer;

/**
 * This class keeps track of services which have a particular service type and which belongs to a particular osgi
 * context, and provides a common functionality for each tracked service while publishing.
 *
 * @param <T> Service type to be tracked
 */
public class EventPublisher<T> {

    private final ServiceTracker serviceTracker;

    /**
     * Constructor of the EventPublisher.
     *
     * @param bundleContext Context of the tracking services.
     * @param listenerType  Service type to be tracked.
     */
    public EventPublisher(BundleContext bundleContext, Class<T> listenerType) {
        this.serviceTracker = new ServiceTracker<T, T>(bundleContext, listenerType, null);
        this.serviceTracker.open();
    }

    /**
     * Provide a common functionality for each tracked service.
     *
     * @param consumer Functionality to be served.
     */
    public void publish(Consumer<T> consumer) {
        T[] services = (T[]) this.serviceTracker.getServices();
        if (services != null) {
            for (T service : services) {
                consumer.accept(service);
            }
        }
    }

    /**
     * Stop tracking services when this object is garbage collected.
     *
     * @throws Throwable the {@code Exception} raised by this method
     */
    @Override
    protected void finalize() throws Throwable {
        this.serviceTracker.close();
        super.finalize();
    }
}
