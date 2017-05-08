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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.RestApi;
import org.wso2.carbon.uuf.internal.exception.RestApiDeploymentException;
import org.wso2.msf4j.Microservice;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Deploys UI-specific REST APIs as OSGi services.
 * This implementation depends on the OSGi mode of MSF4J. REST APIs that are deployed should be {@link Microservice}s.
 *
 * @since 1.0.0
 */
public class OsgiRestApiDeployer implements RestApiDeployer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsgiRestApiDeployer.class);

    private final BundleContext bundleContext;

    public OsgiRestApiDeployer(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * {@inheritDoc}
     *
     * @throws RestApiDeploymentException if this deployer is inactive or {@code restApi} is not an instance of {@link
     *                                    Microservice}
     */
    @Override
    public void deploy(RestApi restApi, String apiContextPath) throws RestApiDeploymentException {
        if (bundleContext == null) {
            throw new RestApiDeploymentException(
                    "Cannot deploy UI-specific REST API '" + restApi + "' for context path '" + apiContextPath +
                            "' as the REST API deployer '" + this.getClass().getName() + "' is inactive.");
        }
        if (!(restApi instanceof Microservice)) {
            throw new RestApiDeploymentException(
                    "Cannot deploy UI-specific REST API '" + restApi + "' for context path '" + apiContextPath +
                            "' as it doesn't implement '" + Microservice.class.getName() + "'.");
        }

        Dictionary<String, String> properties = new Hashtable<>();
        properties.put("contextPath", apiContextPath);
        bundleContext.registerService(Microservice.class, (Microservice) restApi, properties);
        LOGGER.debug("Instance of '{}' registered as a microservice for context path '{}'.",
                     restApi.getClass().getName(), restApi);
    }
}
