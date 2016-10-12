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

package org.wso2.carbon.uuf.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.RequestDispatcher;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.UUFAppRegistry;

import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_BAD_REQUEST;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_NOT_FOUND;

/**
 * Each MSF4JHttpConnector has a ServerConnection, which provides a connection between the UUFMicroservice and the
 * application's contextPath. This class will be called from the UUFMicroservice for each MicroserviceHttpRequest to
 * serve an application.
 */
public class ServerConnection {

    private static final Logger log = LoggerFactory.getLogger(ServerConnection.class);

    private final String contextPath;
    private final RequestDispatcher requestDispatcher;
    private final UUFAppRegistry uufAppRegistry;

    /**
     * Constructor of ServerConnection.
     *
     * @param contextPath    Application's context path.
     * @param uufAppRegistry Instance of UUFAppRegistry. Used to retrieve the relevant application from the registry.
     */
    public ServerConnection(String contextPath, UUFAppRegistry uufAppRegistry) {
        this.requestDispatcher = new RequestDispatcher();
        this.contextPath = contextPath;
        this.uufAppRegistry = uufAppRegistry;
    }

    /**
     * The method that will be called from the UUFMicroservice to serve an application.
     *
     * @param request  Request for serving the application.
     * @param response Response to be send.
     */
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
            app = uufAppRegistry.getApp(request.getContextPath()).orElse(null);
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

    /**
     * Return the context path of the application, which has this connection for.
     *
     * @return Application's context path.
     */
    public String getContextPath() {
        return this.contextPath;
    }
}
