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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.uuf.api.Connector;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.exception.HttpErrorException;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_BAD_REQUEST;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_NOT_FOUND;

/**
 * OSGi service component for UUFServer.
 */
@Component(
        name = "UUFServerSC",
        immediate = true,
        service = RequiredCapabilityListener.class
)
public class UUFServiceComponent {

    private static final Logger log = LoggerFactory.getLogger(UUFServiceComponent.class);

    private final AtomicInteger count = new AtomicInteger(0);
    private UUFAppDeployer appDeployer;
    private final RequestDispatcher requestDispatcher;

    public UUFServiceComponent() {
        this(null, new RequestDispatcher());
    }

    public UUFServiceComponent(UUFAppDeployer appDeployer, RequestDispatcher requestDispatcher) {
        this.appDeployer = appDeployer;
        this.requestDispatcher = requestDispatcher;
    }

    @Activate
    public void activate() {
        log.info("UUFServer service component activated.");
    }

    @Deactivate
    public void deactivate() {
        log.info("UUFServer service component deactivated.");
    }

    @Reference(name = "deployer",
               service = UUFAppDeployer.class,
               cardinality = ReferenceCardinality.MANDATORY,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetAppDeployer")
    @SuppressWarnings("unused")
    public void setAppDeployer(UUFAppDeployer appDeployer) {
        this.appDeployer = appDeployer;
        log.info("UUF App Deployer '" + appDeployer + "' registered.");
    }

    public void unsetAppDeployer(UUFAppDeployer appDeployer) {
        log.info("sasss");
    }

    /**
     * This bind method is invoked by OSGi framework whenever a new Connector is registered.
     *
     * @param connector registered connector
     */
    @Reference(name = "connector",
               service = Connector.class,
               cardinality = ReferenceCardinality.AT_LEAST_ONE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetConnector")
    @SuppressWarnings("unused")
    protected void setConnector(Connector connector) {
        connector.setRequestServer((request, response) -> {
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
    protected void unsetConnector(Connector connector) {
        log.info("Connector '" + connector.getClass().getName() + "' unregistered.");
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
