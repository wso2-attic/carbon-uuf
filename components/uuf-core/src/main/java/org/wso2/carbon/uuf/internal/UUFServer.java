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
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.spi.HttpConnector;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.UUFAppDeployer;

import java.util.concurrent.atomic.AtomicInteger;

import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_BAD_REQUEST;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_NOT_FOUND;

/**
 * OSGi service component for UUFServer.
 */
@Component(name = "org.wso2.carbon.uuf.internal.UUFServer",
           immediate = true
)
@SuppressWarnings("unused")
public class UUFServer {

    private static final boolean DEV_MODE_ENABLED;
    private static final Logger log = LoggerFactory.getLogger(UUFServer.class);

    static {
        DEV_MODE_ENABLED = Boolean.parseBoolean(System.getProperties().getProperty("devmode", "false"));
    }

    private final AtomicInteger count = new AtomicInteger(0);
    private final RequestDispatcher requestDispatcher;
    private final HttpConnectorTracker<HttpConnector> httpConnectorTracker;
    private UUFAppDeployer appDeployer;

    public UUFServer() {
        this(new RequestDispatcher());
    }

    public UUFServer(RequestDispatcher requestDispatcher) {
        this.requestDispatcher = requestDispatcher;
        this.httpConnectorTracker = new HttpConnectorTracker<>(
                FrameworkUtil.getBundle(UUFServer.class).getBundleContext(),
                HttpConnector.class);
        httpConnectorTracker.open();
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
    public void setHttpConnector(HttpConnector connector) {
        connector.setServerConnection((request, response) -> {
            MDC.put("uuf-request", String.valueOf(count.incrementAndGet()));
            serve(request, response);
            MDC.remove("uuf-request");
            return response;
        });
        log.info("HttpConnector '" + connector.getClass().getName() + "' registered.");
    }

    /**
     * This bind method is invoked by OSGi framework whenever a Connector is left.
     *
     * @param connector unregistered connector
     */
    public void unsetHttpConnector(HttpConnector connector) {
        connector.setServerConnection(null);
        log.info("HttpConnector '" + connector.getClass().getName() + "' unregistered.");
    }

    /**
     * This bind method is invoked by OSGi framework whenever a new UUFAppDeployer is registered.
     *
     * @param uufAppDeployer registered uuf app registry creator
     */
    @Reference(name = "uufAppDeployer",
               service = UUFAppDeployer.class,
               cardinality = ReferenceCardinality.MANDATORY,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetUUFAppRegistry")
    public void setUUFAppRegistry(UUFAppDeployer uufAppDeployer) {
        this.appDeployer = uufAppDeployer;
        this.appDeployer.SetHttpConnectorTracker(httpConnectorTracker);
        log.debug("UUFAppDeployer '" + uufAppDeployer.getClass().getName() + "' registered.");
    }

    /**
     * This bind method is invoked by OSGi framework whenever a UUFAppDeployer is left.
     *
     * @param uufAppDeployer unregistered uuf app registry
     */
    public void unsetUUFAppRegistry(UUFAppDeployer uufAppDeployer) {
        this.appDeployer = null;
        log.debug("UUFAppDeployer " + uufAppDeployer.getClass().getName() + " unregistered.");
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        log.debug("UUFServer service activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        httpConnectorTracker.close();
        log.debug("UUFServer service deactivated.");
    }

    public void serve(HttpRequest request, HttpResponse response) {
        if (!request.isValid()) {
            requestDispatcher.serveDefaultErrorPage(STATUS_BAD_REQUEST, "Invalid URI '" + request.getUri() + "'.",
                                                    response);
            return;
        }
        if (request.isDefaultFaviconRequest()) {
            requestDispatcher.serveDefaultFavicon(request, response);
            return;
        }

        App app = null;
        try {
            app = appDeployer.getApp(request.getContextPath()).orElse(null);
        } catch (UUFException e) {
            String msg = "A server error occurred while serving for request '" + request + "'.";
            log.error(msg, e);
            requestDispatcher.serveDefaultErrorPage(STATUS_INTERNAL_SERVER_ERROR, msg, response);
        } catch (Exception e) {
            String msg = "An unexpected error occurred while serving for request '" + request + "'.";
            log.error(msg, e);
            requestDispatcher.serveDefaultErrorPage(STATUS_INTERNAL_SERVER_ERROR, msg, response);
        }

        if (app == null) {
            requestDispatcher.serveDefaultErrorPage(STATUS_NOT_FOUND, "Cannot find an app for context path '" +
                    request.getContextPath() + "'.", response);
        } else {
            requestDispatcher.serve(app, request, response);
        }
    }

    @Deprecated
    public static boolean isDevModeEnabled() {
        // TODO: 8/13/16 Remove this when Carbon 'Utils.isDevModeEnabled()' is available in C5.20
        return DEV_MODE_ENABLED;
    }
}
