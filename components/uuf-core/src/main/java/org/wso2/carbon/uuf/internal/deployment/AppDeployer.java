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

import org.wso2.carbon.uuf.core.App;

import java.util.Set;

/**
 * A deployer for UUF apps.
 *
 * @since 1.0.0
 */
public interface AppDeployer {

    /**
     * Deploys all the available apps.
     *
     * @return context paths of the deployed apps.
     */
    Set<String> deploy();

    /**
     * Returns the deployed app corresponds for the specified context path.
     *
     * @param contextPath context path of the app
     * @return deployed app or {@code null} is there is no app associated with the specified context path
     */
    App getApp(String contextPath);
}
