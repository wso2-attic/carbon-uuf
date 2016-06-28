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

import org.apache.commons.lang3.tuple.Pair;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.core.create.AppCreator;
import org.wso2.carbon.uuf.internal.core.create.ClassLoaderProvider;
import org.wso2.carbon.uuf.internal.io.ArtifactAppReference;
import org.wso2.carbon.uuf.internal.io.BundleClassLoaderProvider;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.spi.RenderableCreator;
import org.wso2.carbon.uuf.spi.UUFAppRegistry;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UUF Deployer.
 */
@Component(
        name = "org.wso2.carbon.uuf.internal.UUFAppDeployer",
        immediate = true,
        service = RequiredCapabilityListener.class,
        property = {
                "componentName=wso2-uuf-deployer"
        }
)
@SuppressWarnings("unused")
public class UUFAppDeployer implements Deployer, UUFAppRegistry, RequiredCapabilityListener {

    private static final Logger log = LoggerFactory.getLogger(UUFAppDeployer.class);

    private final ArtifactType artifactType;
    private final URL location;
    private final Map<String, App> deployedApps;
    private final Map<String, Artifact> pendingToDeployArtifacts;
    private final Object lock;
    private final Set<RenderableCreator> renderableCreators;
    private final ClassLoaderProvider classLoaderProvider;
    private AppCreator appCreator;
    private BundleContext bundleContext;

    public UUFAppDeployer() {
        this.artifactType = new ArtifactType<>("uufapp");
        try {
            this.location = new URL("file:uufapps");
        } catch (MalformedURLException e) {
            throw new UUFException("Cannot create URL 'file:uufapps'.", e);
        }
        this.deployedApps = new ConcurrentHashMap<>();
        this.pendingToDeployArtifacts = new ConcurrentHashMap<>();
        this.lock = new Object();
        this.renderableCreators = ConcurrentHashMap.newKeySet();
        this.classLoaderProvider = new BundleClassLoaderProvider();
    }

    @Override
    public void init() {
        log.debug("UUFAppDeployer initialized.");
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }

    @Override
    public URL getLocation() {
        return location;
    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        Pair<String, String> appNameContextPath = getAppNameContextPath(artifact);
        if (deployedApps.containsKey(appNameContextPath.getRight())) {
            throw new CarbonDeploymentException(
                    "Cannot deploy UUF app artifact in '" + artifact.getPath() + "' for context path '" +
                            appNameContextPath.getRight() +
                            "' as another app is already registered for the same context path.");
        }

        pendingToDeployArtifacts.put(appNameContextPath.getRight(), artifact);
        log.debug("UUF app '" + appNameContextPath.getLeft() + "' added to the pending deployments list.");
        return appNameContextPath.getLeft();
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        Pair<String, String> appNameContextPath = getAppNameContextPath(artifact);
        if (deployedApps.containsKey(appNameContextPath.getRight())) {
            // This artifact is already deployed.
            App createdApp = appCreator.createApp(new ArtifactAppReference(Paths.get(artifact.getPath())));
            deployedApps.put(createdApp.getContextPath(), createdApp);
            log.info("UUF app '" + createdApp.getName() + "' re-deployed for context path '" +
                             createdApp.getContextPath() + "'.");
            return createdApp.getName();
        } else {
            // This artifact is not deployed yet. It is in the pending list 'pendingToDeployArtifacts'. So new
            // changes will be picked up when it is actually deployed.
            return appNameContextPath.getLeft();
        }
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        String appName = (String) key;
        Optional<App> removedApp = deployedApps.values().stream()
                .filter(app -> app.getName().equals(appName))
                .findFirst()
                .map(removingApp -> deployedApps.remove(removingApp.getContextPath()));
        if (removedApp.isPresent()) {
            // App with 'appName' is deployed.
            log.info("UUF app '" + removedApp.get().getName() + "' undeployed for context '" +
                             removedApp.get().getContextPath() + "'.");
        } else {
            // App with 'appName' is not deployed yet.
            for (Map.Entry<String, Artifact> entry : pendingToDeployArtifacts.entrySet()) {
                Pair<String, String> appNameContextPath = getAppNameContextPath(entry.getValue());
                if (appName.equals(appNameContextPath.getLeft())) {
                    Artifact removedArtifact = pendingToDeployArtifacts.remove(appNameContextPath.getRight());
                    log.info("UUF app in '" + removedArtifact.getPath() + "' removed even before it deployed.");
                    break;
                }
            }
        }
    }

    private Pair<String, String> getAppNameContextPath(Artifact artifact) {
        // TODO: 6/28/16 deployment.properties can override app's context path
        // Fully qualified name of the app is equals to the name od the app directory. This is guaranteed by the UUF
        // Maven plugin.
        String appFullyQualifiedName = Paths.get(artifact.getPath()).getFileName().toString();
        return Pair.of(appFullyQualifiedName, ("/" + NameUtils.getSimpleName(appFullyQualifiedName)));
    }

    private App deployApp(String contextPath) {
        App app;
        synchronized (lock) {
            Artifact artifact = pendingToDeployArtifacts.remove(contextPath);
            if (artifact == null) {
                // App is deployed before acquiring the lock.
                return deployedApps.get(contextPath);
            }
            if (!artifact.getFile().exists()) {
                // Somehow artifact has been removed/deleted. So we cannot create an app from it.
                log.warn("Cannot deploy UUF app in '" + artifact.getPath() + "' as it does not exists anymore.");
                return null;
            }
            app = appCreator.createApp(new ArtifactAppReference(Paths.get(artifact.getPath())));
            deployedApps.put(app.getContextPath(), app);
        }
        log.info("UUF app '" + app.getName() + "' deployed for context path '" + app.getContextPath() + "'.");
        return app;
    }

    @Override
    public Optional<App> getApp(String contextPath) {
        App app = deployedApps.get(contextPath);
        if (app != null) {
            return Optional.of(app);
        } else {
            if (pendingToDeployArtifacts.containsKey(contextPath)) {
                return Optional.ofNullable(deployApp(contextPath));
            } else {
                return Optional.empty();
            }
        }
    }

    /**
     * This bind method is invoked by OSGi framework whenever a new RenderableCreator is registered.
     *
     * @param renderableCreator registered renderable creator
     */
    @Reference(name = "renderableCreator",
               service = RenderableCreator.class,
               cardinality = ReferenceCardinality.AT_LEAST_ONE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetRenderableCreator")
    public void setRenderableCreator(RenderableCreator renderableCreator) {
        if (!renderableCreators.add(renderableCreator)) {
            throw new IllegalArgumentException(
                    "A RenderableCreator for '" + renderableCreator.getSupportedFileExtensions() +
                            "' extensions is already registered");
        }
        log.info("RenderableCreator '" + renderableCreator.getClass().getName() + "' registered for " +
                         renderableCreator.getSupportedFileExtensions() + " extensions.");
    }

    /**
     * This bind method is invoked by OSGi framework whenever a RenderableCreator is left.
     *
     * @param renderableCreator unregistered renderable creator
     */
    public void unsetRenderableCreator(RenderableCreator renderableCreator) {
        renderableCreators.remove(renderableCreator);
        log.info("RenderableCreator " + renderableCreator.getClass().getName() + " unregistered for " +
                         renderableCreator.getSupportedFileExtensions() + " extensions.");
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        log.debug("UUFAppDeployer service activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        this.bundleContext = null;
        log.debug("UUFAppDeployer service deactivated.");
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        this.appCreator = new AppCreator(renderableCreators, classLoaderProvider);
        log.debug("AppCreator is ready.");
        bundleContext.registerService(Deployer.class, this, null);
        log.debug("UUFAppDeployer registered as a Carbon artifact deployer.");
    }
}
