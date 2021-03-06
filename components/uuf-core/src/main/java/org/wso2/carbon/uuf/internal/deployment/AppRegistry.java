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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.reference.AppReference;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.internal.exception.AppCreationException;
import org.wso2.carbon.uuf.internal.exception.DeploymentException;
import org.wso2.carbon.uuf.internal.exception.FileOperationException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A registry that maintains deployed UUF apps.
 *
 * @since 1.0.0
 */
public class AppRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppRegistry.class);

    private final AppFinder appFinder;
    private final AppCreator appCreator;
    private final ConcurrentMap<String, App> deployedApps;

    /**
     * Creates a new app registry with the specified app finder and app creator.
     *
     * @param appFinder  app finder to be used
     * @param appCreator app creator to be used
     */
    public AppRegistry(AppFinder appFinder, AppCreator appCreator) {
        this.appFinder = appFinder;
        this.appCreator = appCreator;
        this.deployedApps = new ConcurrentHashMap<>();
    }

    /**
     * Returns the app for the given context path.
     *
     * @param appContextPath app's context path
     * @return if present the app for the given context path, otherwise {@code null}
     * @throws DeploymentException if some error occurred during app deployment
     */
    public App getApp(String appContextPath) throws DeploymentException {
        return deployedApps.computeIfAbsent(appContextPath, this::createApp);
    }

    /**
     * Clears all the deployed apps of this registry.
     */
    public void clear() {
        deployedApps.clear();
    }

    private App createApp(String appContextPath) throws DeploymentException {
        AppReference appReference;
        try {
            appReference = appFinder.getAppReference(appContextPath).orElse(null);
        } catch (FileOperationException e) {
            throw new DeploymentException("Cannot load an app for context path '" + appContextPath + "'.", e);
        }
        if (appReference == null) {
            return null; // no app found for the given context path
        }

        App app;
        try {
            app = appCreator.createApp(appReference, appContextPath);
        } catch (AppCreationException | FileOperationException e) {
            throw new DeploymentException("Cannot create app '" + appReference.getName() +
                                                  "' to deploy for context path '" + appContextPath + "'.", e);
        } catch (Exception e) {
            throw new DeploymentException(
                    "Cannot deploy app '" + appReference.getName() + "' for context path '" + appContextPath + "'.", e);
        }
        LOGGER.info("UUF app '{}' deployed for context path '{}' successfully.", app.getName(), app.getContextPath());
        return app;
    }
}
