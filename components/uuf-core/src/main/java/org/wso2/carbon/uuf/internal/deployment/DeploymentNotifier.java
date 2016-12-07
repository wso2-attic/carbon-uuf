/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.uuf.spi.HttpConnector;

import java.util.Set;

/**
 * A notifier that notify app deployment event to others.
 *
 * @since 1.0.0
 */
public abstract class DeploymentNotifier {

    /**
     * Returns the HTTP connectors that need to notified.
     *
     * @return HTTP connectors
     */
    protected abstract Set<HttpConnector> getHttpConnectors();

    /**
     * Notifies all the HTTP connectors that the specified context paths are available now.
     *
     * @param deployedAppContextPaths context path of apps
     * @see #getHttpConnectors()
     */
    public void notify(Set<String> deployedAppContextPaths) {
        for (HttpConnector httpConnector : getHttpConnectors()) {
            for (String deployedAppContextPath : deployedAppContextPaths) {
                httpConnector.registerAppContextPath(deployedAppContextPath);
            }
        }
    }
}
