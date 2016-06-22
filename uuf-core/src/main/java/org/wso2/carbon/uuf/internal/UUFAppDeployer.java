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
import org.wso2.carbon.uuf.spi.RenderableCreator;
import org.wso2.carbon.uuf.spi.UUFAppRegistry;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
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
public class UUFAppDeployer implements Deployer, RequiredCapabilityListener {

    private static final Logger log = LoggerFactory.getLogger(UUFAppDeployer.class);

    private final ArtifactType artifactType;
    private final URL location;
    private volatile AppCreator appCreator;
    private UUFAppRegistry uufAppRegistry;
    private final Set<RenderableCreator> renderableCreators;
    private final ClassLoaderProvider classLoaderProvider;
    private BundleContext bundleContext;

    public UUFAppDeployer() {
        this.renderableCreators = ConcurrentHashMap.newKeySet();
        this.classLoaderProvider = new BundleClassLoaderProvider();
        this.artifactType = new ArtifactType<>("uufapp");
        try {
            this.location = new URL("file:uufapps");
        } catch (MalformedURLException e) {
            throw new UUFException("Cannot create URL 'file:uufapps'.", e);
        }
    }


    @Activate
    @SuppressWarnings("unused")
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        log.debug("UUFAppDeployer service activated.");
    }

    @Deactivate
    @SuppressWarnings("unused")
    protected void deactivate(BundleContext bundleContext) {
        this.bundleContext = null;
        log.debug("UUFAppDeployer service deactivated.");
    }

    /**
     * This bind method is invoked by OSGi framework whenever a new UUFAppRegistry is registered.
     *
     * @param uufAppRegistry registered uuf app registry creator
     */
    @Reference(name = "uufAppRegistry",
               service = UUFAppRegistry.class,
               cardinality = ReferenceCardinality.MANDATORY,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetUUFAppRegistry")
    @SuppressWarnings("unused")
    public void setUUFAppRegistry(UUFAppRegistry uufAppRegistry) {
        this.uufAppRegistry = uufAppRegistry;
        log.info("UUFAppRegistry '" + uufAppRegistry.getClass().getName() + "' registered.");
    }

    /**
     * This bind method is invoked by OSGi framework whenever a UUFAppRegistry is left.
     *
     * @param uufAppRegistry unregistered uuf app registry
     */
    @SuppressWarnings("unused")
    public void unsetUUFAppRegistry(UUFAppRegistry uufAppRegistry) {
        this.uufAppRegistry = null;
        log.info("UUFAppRegistry " + uufAppRegistry.getClass().getName() + " unregistered.");
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public void unsetRenderableCreator(RenderableCreator renderableCreator) {
        renderableCreators.remove(renderableCreator);
        log.info("RenderableCreator " + renderableCreator.getClass().getName() + " unregistered for " +
                         renderableCreator.getSupportedFileExtensions() + " extensions.");
    }

    @Override
    public void init() {
        log.debug("UUFAppDeployer initialized.");
    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        App app = appCreator.createApp(new ArtifactAppReference(Paths.get(artifact.getPath())));
        uufAppRegistry.add(app);
        log.info("App '" + app.getName() + "' deployed for context '" + app.getContext() + "'.");
        return app.getName();
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        String appName = (String) key;
        uufAppRegistry.remove(appName).ifPresent(removedApp -> log.info("App '" + removedApp.getName()
                                                                                + "' undeployed for context '"
                                                                                + removedApp.getContext() + "'."));
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        App app = appCreator.createApp(new ArtifactAppReference(Paths.get(artifact.getPath())));
        uufAppRegistry.add(app);
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

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        this.appCreator = new AppCreator(renderableCreators, classLoaderProvider);
        log.info("AppCreator is ready.");
        bundleContext.registerService(Deployer.class, this, null);
    }
}
