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

package org.wso2.carbon.uuf.internal.io;

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
import org.wso2.carbon.uuf.internal.EventPublisher;
import org.wso2.carbon.uuf.internal.UUFServer;
import org.wso2.carbon.uuf.internal.core.create.AppCreator;
import org.wso2.carbon.uuf.internal.core.create.ClassLoaderProvider;
import org.wso2.carbon.uuf.internal.io.util.ZipArtifactHandler;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.spi.HttpConnector;
import org.wso2.carbon.uuf.spi.RenderableCreator;
import org.wso2.carbon.uuf.spi.UUFAppDeployer;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * UUF app deployer.
 */
@Component(
        name = "org.wso2.carbon.uuf.internal.io.ArtifactAppDeployer",
        immediate = true,
        service = RequiredCapabilityListener.class,
        property = {
                "componentName=wso2-uuf-deployer"
        }
)
@SuppressWarnings("unused")
public class ArtifactAppDeployer implements Deployer, UUFAppDeployer, RequiredCapabilityListener {

    private static final Logger log = LoggerFactory.getLogger(ArtifactAppDeployer.class);

    private final ArtifactType artifactType;
    private final URL location;
    private final ConcurrentMap<String, App> deployedApps;
    private final ConcurrentMap<String, AppArtifact> pendingToDeployArtifacts;
    private final Object lock;
    private final Set<RenderableCreator> renderableCreators;
    private final ClassLoaderProvider classLoaderProvider;
    private EventPublisher eventPublisher;
    private AppCreator appCreator;
    private BundleContext bundleContext;

    public ArtifactAppDeployer() {
        this(new BundleClassLoaderProvider());
    }

    public ArtifactAppDeployer(ClassLoaderProvider classLoaderProvider) {
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
        this.classLoaderProvider = classLoaderProvider;
    }


    @Override
    public void init() {
        log.debug("ArtifactAppDeployer initialized.");
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
        Pair<String, String> appNameContextPath;
        try {
            appNameContextPath = getAppNameContextPath(artifact);
        } catch (Exception e) {
            // catching any/all exception/s
            throw new CarbonDeploymentException("Cannot deploy UUF app artifact in " + artifact.getPath() + ".", e);
        }
        if (deployedApps.containsKey(appNameContextPath.getRight())) {
            throw new CarbonDeploymentException(
                    "Cannot deploy UUF app artifact in '" + artifact.getPath() + "' for context path '" +
                            appNameContextPath.getRight() +
                            "' as another app is already registered for the same context path.");
        }

        for (Object o : eventPublisher.getServiceTracker().getServices()) {
            HttpConnector httpConnector = (HttpConnector) o;
            httpConnector.registerContextPath(appNameContextPath.getRight());
        }

        pendingToDeployArtifacts.put(appNameContextPath.getRight(), new AppArtifact(appNameContextPath.getLeft(),
                                                                                    artifact));
        log.debug("UUF app '" + appNameContextPath.getLeft() + "' added to the pending deployments list.");
        return appNameContextPath.getLeft();
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        try {
            Pair<String, String> appNameContextPath = getAppNameContextPath(artifact);
            if (deployedApps.containsKey(appNameContextPath.getRight())) {
                // This artifact is already deployed.
                App createdApp = createApp(appNameContextPath.getLeft(), appNameContextPath.getRight(), artifact);
                deployedApps.put(createdApp.getContextPath(), createdApp);
                log.info("UUF app '" + createdApp.getName() + "' re-deployed for context path '" +
                                 createdApp.getContextPath() + "'.");
                return createdApp.getName();
            } else {
            /* This artifact is not deployed yet. It is in the pending list 'pendingToDeployArtifacts'. So new
            changes will be picked up when it is actually deployed.*/
                return appNameContextPath.getLeft();
            }
        } catch (Exception e) {
            // catching any/all exception/s
            throw new CarbonDeploymentException(
                    "An error occurred while re-deploying UUF app in '" + artifact.getPath() + "'.", e);
        }
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        String appName = (String) key;

        for (App app : deployedApps.values()) {
            if (app.getName().equals(appName)) {
                // App with 'appName' is deployed.
                deployedApps.remove(app.getContextPath());
                log.info("UUF app '" + app.getName() + "' undeployed for context '" + app.getContextPath() + "'.");
                return;
            }
        }

        // App with 'appName' is not deployed yet.
        for (Map.Entry<String, AppArtifact> entry : pendingToDeployArtifacts.entrySet()) {
            AppArtifact appArtifact = entry.getValue();
            if (appArtifact.appName.equals(appName)) {
                pendingToDeployArtifacts.remove(entry.getKey());
                log.info("UUF app in '" + appArtifact.artifact.getPath() + "' removed even before it deployed.");
                return;
            }
        }
    }

    private Pair<String, String> getAppNameContextPath(Artifact artifact) {
        // Fully qualified name of the app is equals to the name of the app directory. This is guaranteed by the UUF
        // Maven plugin.
        String appFullyQualifiedName;
        if (ZipArtifactHandler.isZipArtifact(artifact)) {
            appFullyQualifiedName = ZipArtifactHandler.getAppName(artifact);
        } else {
            appFullyQualifiedName = Paths.get(artifact.getPath()).getFileName().toString();
        }
        // TODO: 6/28/16 deployment.properties can override app's context path
        return Pair.of(appFullyQualifiedName, ("/" + NameUtils.getSimpleName(appFullyQualifiedName)));
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

    private App deployApp(String contextPath) {
        App createdApp;
        synchronized (lock) {
            AppArtifact appArtifact = pendingToDeployArtifacts.remove(contextPath);
            Artifact artifact = appArtifact.artifact;
            if (artifact == null) {
                // App is deployed before acquiring the lock.
                return deployedApps.get(contextPath);
            }
            if (!artifact.getFile().exists()) {
                // Somehow artifact has been removed/deleted. So we cannot create an app from it.
                log.warn("Cannot deploy UUF app in '" + artifact.getPath() + "' as it does not exists anymore.");
                return null;
            }
            try {
                createdApp = createApp(appArtifact.appName, contextPath, artifact);
            } catch (Exception e) {
                // catching any/all exception/s
                if (UUFServer.isDevModeEnabled()) {
                    /* If the server is in the developer mode, add the artifact back to the 'pendingToDeployArtifacts'
                    map so the developer can correct the error and attempt to re-deploy the artifact. */
                    pendingToDeployArtifacts.put(contextPath, appArtifact);
                }
                throw new UUFException("An error occurred while deploying UUF app in '" + artifact.getPath() + "'.", e);
            }
            deployedApps.put(createdApp.getContextPath(), createdApp);
        }
        log.info("UUF app '" + createdApp.getName() + "' deployed for context path '" + createdApp.getContextPath() +
                         "'.");
        return createdApp;
    }

    private App createApp(String appName, String appContextPath, Artifact artifact) {
        ArtifactAppReference appReference;
        if (ZipArtifactHandler.isZipArtifact(artifact)) {
            appReference = new ArtifactAppReference(ZipArtifactHandler.unzip(artifact, appName));
        } else {
            appReference = new ArtifactAppReference(Paths.get(artifact.getPath()));
        }
        return appCreator.createApp(appReference, appContextPath);
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
        eventPublisher = new EventPublisher(bundleContext, HttpConnector.class);
        eventPublisher.getServiceTracker().open();
        log.debug("ArtifactAppDeployer service activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        this.bundleContext = null;
        eventPublisher.getServiceTracker().close();
        log.debug("ArtifactAppDeployer service deactivated.");
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        this.appCreator = new AppCreator(renderableCreators, classLoaderProvider);
        log.debug("AppCreator is ready.");

        bundleContext.registerService(Deployer.class, this, null);
        log.info("UUF ArtifactAppDeployer registered as a Carbon artifact deployer.");

        bundleContext.registerService(UUFAppDeployer.class, this, null);
        log.debug("ArtifactAppDeployer registered as an UUFAppDeployer.");
    }

    private static class AppArtifact {

        private final String appName;
        private final Artifact artifact;

        public AppArtifact(String appName, Artifact artifact) {
            this.appName = appName;
            this.artifact = artifact;
        }
    }
}
