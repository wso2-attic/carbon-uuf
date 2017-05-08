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

import org.apache.commons.lang3.tuple.Pair;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.uuf.spi.HttpConnector;

import java.util.List;

/**
 * A notifier that notify app deployment event to {@link HttpConnector}s.
 *
 * @since 1.0.0
 */
public class HttpConnectorDeploymentNotifier implements DeploymentNotifier {

    private final ServiceTracker<HttpConnector, HttpConnector> serviceTracker;

    public HttpConnectorDeploymentNotifier(BundleContext bundleContext) {
        this.serviceTracker = new ServiceTracker<>(bundleContext, HttpConnector.class, null);
        this.serviceTracker.open();
    }

    /**
     * Notifies available {@link HttpConnector}s about the availability of the specified apps
     *
     * @param appNamesContextPaths names and context paths of the available apps
     */
    @Override
    public void notify(List<Pair<String, String>> appNamesContextPaths) {
        HttpConnector[] httpConnectors = serviceTracker.getServices(new HttpConnector[serviceTracker.size()]);
        for (HttpConnector httpConnector : httpConnectors) {
            for (Pair<String, String> appNameContextPath : appNamesContextPaths) {
                httpConnector.registerApp(appNameContextPath.getLeft(), appNameContextPath.getRight());
            }
        }
    }
}
