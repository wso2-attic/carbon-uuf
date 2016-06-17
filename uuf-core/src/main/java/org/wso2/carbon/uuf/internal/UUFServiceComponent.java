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
import org.slf4j.MDC;
import org.wso2.carbon.kernel.deployment.Deployer;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.uuf.spi.HttpConnector;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.exception.HttpErrorException;
import org.wso2.carbon.uuf.internal.core.create.AppCreator;
import org.wso2.carbon.uuf.internal.core.create.ClassLoaderProvider;
import org.wso2.carbon.uuf.internal.io.BundleClassLoaderProvider;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.RenderableCreator;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_BAD_REQUEST;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_NOT_FOUND;

/**
 * OSGi service component for UUFServer.
 */
@Component(name = "UUFServiceComponent",
           immediate = true,
           service = RequiredCapabilityListener.class,
           property = {
                   "capability-name=org.wso2.carbon.uuf.spi.RenderableCreator,org.wso2.carbon.uuf.api.Connector",
                   "component-key=wso2-uuf-service"
           }
)
public class UUFServiceComponent implements RequiredCapabilityListener {

    private static final Logger log = LoggerFactory.getLogger(UUFServiceComponent.class);

    private final AtomicInteger count = new AtomicInteger(0);
    private final Set<RenderableCreator> renderableCreators;
    private final UUFAppDeployer appDeployer;
    private final RequestDispatcher requestDispatcher;
    private final ClassLoaderProvider classLoaderProvider;
    private BundleContext bundleContext;

    public UUFServiceComponent() {
        this(new UUFAppDeployer(), new RequestDispatcher());
    }

    public UUFServiceComponent(UUFAppDeployer appDeployer, RequestDispatcher requestDispatcher) {
        this.renderableCreators = ConcurrentHashMap.newKeySet();
        this.appDeployer = appDeployer;
        this.requestDispatcher = requestDispatcher;
        this.classLoaderProvider = new BundleClassLoaderProvider();
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
        appDeployer.setAppCreator(new AppCreator(renderableCreators, classLoaderProvider));
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

    /**
     * This bind method is invoked by OSGi framework whenever a new Connector is registered.
     *
     * @param connector registered connector
     */
    @Reference(name = "httpConnector",
               service = HttpConnector.class,
               cardinality = ReferenceCardinality.AT_LEAST_ONE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetHttpConnector")
    @SuppressWarnings("unused")
    public void setHttpConnector(HttpConnector connector) {
        connector.setServerConnection((request, response) -> {
            MDC.put("uuf-request", String.valueOf(count.incrementAndGet()));
            serve(request, response);
            MDC.remove("uuf-request");
            return response;
        });
        log.info("Connector '" + connector.getClass().getName() + "' registered.");
    }

    /**
     * This bind method is invoked by OSGi framework whenever a Connector is left.
     *
     * @param connector unregistered connector
     */
    @SuppressWarnings("unused")
    public void unsetHttpConnector(HttpConnector connector) {
        connector.setServerConnection(null);
        log.info("Connector '" + connector.getClass().getName() + "' unregistered.");
    }

    @Activate
    @SuppressWarnings("unused")
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        log.debug("UUFServer service component activated.");
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        bundleContext.registerService(Deployer.class, appDeployer, null);
        log.debug("UUF AppDeployer registered.");
    }

    @Deactivate
    @SuppressWarnings("unused")
    protected void deactivate(BundleContext bundleContext) {
        log.debug("UUFServer service component deactivated.");
    }

    public void serve(HttpRequest request, HttpResponse response) {
        Optional<App> app = Optional.empty();
        try {
            if (!request.isValid()) {
                requestDispatcher.serveErrorPage(request, response, STATUS_BAD_REQUEST,
                                                 "Invalid URI '" + request.getUri() + "'.");
                return;
            }
            if (request.isDefaultFaviconRequest()) {
                requestDispatcher.serveDefaultFavicon(request, response);
                return;
            }

            app = appDeployer.getApp(request.getAppContext());
            if (!app.isPresent()) {
                requestDispatcher.serveErrorPage(request, response, STATUS_NOT_FOUND,
                                                 "Cannot find an app for context '" + request.getAppContext() + "'.");
                return;
            }
            requestDispatcher.serve(app.get(), request, response);
        } catch (Exception e) {
            log.error("An unexpected error occurred while serving for request '" + request + "'.", e);
            requestDispatcher.serveErrorPage((app.isPresent() ? app.get() : null), request, response,
                                             new HttpErrorException(STATUS_INTERNAL_SERVER_ERROR, e.getMessage(), e));
        }
    }
}
