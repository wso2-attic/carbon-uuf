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

class HttpConnectorTracker<S> extends ServiceTracker<S, S> implements
                                                           HttpConnectorServiceAccess<S> {

    HttpConnectorTracker(BundleContext bundleContext, Class<S> trackerClass) {
        super(bundleContext, trackerClass, null);
    }

    /**
     * Executes the passed function {@code func} for each registered service.
     *
     * @param func the closure to be called for this service (receives the service as argument)
     */
    public void forAllServices(Consumer<S> func) {
        S[] services = (S[]) this.getServices();
        for (S service : services) {
            func.accept(service);
        }
    }
}
