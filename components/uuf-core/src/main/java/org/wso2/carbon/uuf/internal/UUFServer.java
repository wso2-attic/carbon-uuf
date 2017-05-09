/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.internal;

import org.apache.commons.lang3.tuple.Pair;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.uuf.api.Server;
import org.wso2.carbon.uuf.internal.deployment.AppCreator;
import org.wso2.carbon.uuf.internal.deployment.AppFinder;
import org.wso2.carbon.uuf.internal.deployment.AppRegistry;
import org.wso2.carbon.uuf.internal.deployment.ClassLoaderProvider;
import org.wso2.carbon.uuf.internal.deployment.DeploymentNotifier;
import org.wso2.carbon.uuf.internal.deployment.HttpConnectorDeploymentNotifier;
import org.wso2.carbon.uuf.internal.deployment.OsgiPluginProvider;
import org.wso2.carbon.uuf.internal.deployment.OsgiRestApiDeployer;
import org.wso2.carbon.uuf.internal.deployment.PluginProvider;
import org.wso2.carbon.uuf.internal.deployment.RestApiDeployer;
import org.wso2.carbon.uuf.internal.io.deployment.ArtifactAppFinder;
import org.wso2.carbon.uuf.internal.io.deployment.BundleClassLoaderProvider;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.RenderableCreator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "org.wso2.carbon.uuf.internal.UUFServer",
           service = RequiredCapabilityListener.class,
           immediate = true,
           property = {
                   "componentName=wso2-uuf-server"
           }
)
public class UUFServer implements Server, RequiredCapabilityListener {

    private static final boolean DEV_MODE_ENABLED = Boolean.getBoolean("devmode");
    private static final Logger LOGGER = LoggerFactory.getLogger(UUFServer.class);

    private AppRegistry appRegistry;
    private final AppFinder appFinder = new ArtifactAppFinder();
    private Set<RenderableCreator> renderableCreators = new HashSet<>();
    private final ClassLoaderProvider classLoaderProvider = new BundleClassLoaderProvider();
    private PluginProvider pluginProvider;
    private RestApiDeployer restApiDeployer;
    private final RequestDispatcher requestDispatcher = new RequestDispatcher();
    private DeploymentNotifier deploymentNotifier;
    private BundleContext bundleContext;
    private ServiceRegistration serverServiceRegistration;

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
    protected void setRenderableCreator(RenderableCreator renderableCreator) {
        if (!renderableCreators.add(renderableCreator)) {
            throw new IllegalArgumentException(
                    "A RenderableCreator for '" + renderableCreator.getSupportedFileExtensions() +
                            "' extensions is already registered");
        }
        LOGGER.debug("RenderableCreator '{}' registered for {} extensions.",
                    renderableCreator.getClass().getName(), renderableCreator.getSupportedFileExtensions());
    }

    /**
     * This bind method is invoked by OSGi framework whenever a RenderableCreator is left.
     *
     * @param renderableCreator unregistered renderable creator
     */
    protected void unsetRenderableCreator(RenderableCreator renderableCreator) {
        renderableCreators.remove(renderableCreator);
        LOGGER.debug("RenderableCreator '{}' unregistered for {} extensions.",
                    renderableCreator.getClass().getName(), renderableCreator.getSupportedFileExtensions());
        if (appRegistry != null) {
            // Remove apps that might have used the removed renderable creator to create.
            appRegistry.clear();
            appRegistry = createAppRegistry();
        }
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        LOGGER.debug("UUFServer activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        stop();
        this.bundleContext = null;
        serverServiceRegistration.unregister();
        pluginProvider = null;
        restApiDeployer = null;
        deploymentNotifier = null;
        appRegistry = null;
        LOGGER.debug("UUFServer deactivated.");
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        serverServiceRegistration = bundleContext.registerService(Server.class, this, null);
        LOGGER.debug("'{}' registered as a Server.", getClass().getName());

        pluginProvider = new OsgiPluginProvider(bundleContext);
        restApiDeployer = new OsgiRestApiDeployer(bundleContext);
        appRegistry = createAppRegistry();
        deploymentNotifier = new HttpConnectorDeploymentNotifier(bundleContext);
    }

    private AppRegistry createAppRegistry() {
        AppCreator appCreator = new AppCreator(renderableCreators, classLoaderProvider, pluginProvider,
                                               restApiDeployer);
        return new AppRegistry(appFinder, appCreator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serve(HttpRequest request, HttpResponse response) {
        requestDispatcher.serve(request, response, appRegistry);
    }

    public void start() {
        List<Pair<String, String>> availableApps = appFinder.getAvailableApps();
        deploymentNotifier.notify(availableApps);
    }

    public void stop() {
        appRegistry.clear();
    }

    public static boolean isDevModeEnabled() {
        return DEV_MODE_ENABLED;
    }
}
