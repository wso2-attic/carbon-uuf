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


import org.wso2.carbon.uuf.api.RestApi;
import org.wso2.carbon.uuf.internal.exception.RestApiDeploymentException;

/**
 * A deployer that can deploy UI-specific REST APIs of an UUF component.
 *
 * @since 1.0.0
 */
public interface RestApiDeployer {

    /**
     * Deploys the given REST API for specified context path.
     *
     * @param restApi        REST API to be deployed
     * @param apiContextPath context path of the REST API to use
     * @throws RestApiDeploymentException if an error occurred during REST API deployment
     */
    void deploy(RestApi restApi, String apiContextPath) throws RestApiDeploymentException;
}
