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

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.core.create.AppCreator;
import org.wso2.carbon.uuf.internal.io.ArtifactAppReference;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


/**
 * UUF Deployer.
 */
public class UUFAppDeployer implements Deployer {

    private static final Logger log = LoggerFactory.getLogger(UUFAppDeployer.class);

    private final ArtifactType artifactType;
    private final URL location;
    private final Map<String, App> apps;
    private volatile AppCreator appCreator;

    public UUFAppDeployer() {
        this.artifactType = new ArtifactType<>("uufapp");
        try {
            this.location = new URL("file:uufapps");
        } catch (MalformedURLException e) {
            throw new UUFException("Cannot create URL 'file:uufapps'.", e);
        }
        this.apps = new ConcurrentHashMap<>();
    }

    @Override
    public void init() {
        log.debug("UUFAppDeployer initialized.");
    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        App app = appCreator.createApp(new ArtifactAppReference(Paths.get(artifact.getPath())));
        apps.put(app.getContext(), app);
        log.info("App '" + app.getName() + "' deployed for context '" + app.getContext() + "'.");
        return app.getName();
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        String appName = (String) key;
        apps.values().stream()
                .filter(app -> app.getName().equals(appName))
                .findFirst()
                .ifPresent(app -> {
                    App removedApp = apps.remove(app.getContext());
                    log.info("App '" + removedApp.getName() + "' undeployed for context '" + app.getContext() + "'.");
                });
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        App app = appCreator.createApp(new ArtifactAppReference(Paths.get(artifact.getPath())));
        apps.put(app.getContext(), app);
        log.info("App '" + app.getName() + "' re-deployed for context '" + app.getContext() + "'.");
        return app.getName();
    }

    @Override
    public URL getLocation() {
        return location;
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }

    public void setAppCreator(AppCreator appCreator) {
        this.appCreator = appCreator;
    }

    public Optional<App> getApp(String contextPath) {
        return Optional.ofNullable(apps.get(contextPath));
    }
}
