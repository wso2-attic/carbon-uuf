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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.HttpRequest;
import org.wso2.carbon.uuf.api.HttpResponse;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.exception.FragmentNotFoundException;
import org.wso2.carbon.uuf.exception.HttpErrorException;
import org.wso2.carbon.uuf.exception.PageNotFoundException;
import org.wso2.carbon.uuf.exception.PageRedirectException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.core.create.AppCreator;
import org.wso2.carbon.uuf.internal.core.create.AppDiscoverer;
import org.wso2.carbon.uuf.internal.debug.Debugger;
import org.wso2.carbon.uuf.internal.io.StaticResolver;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.wso2.carbon.uuf.api.HttpResponse.CONTENT_TYPE_TEXT_HTML;
import static org.wso2.carbon.uuf.api.HttpResponse.HEADER_LOCATION;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_BAD_REQUEST;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_FOUND;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_MOVED_PERMANENTLY;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_NOT_FOUND;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_OK;

public class UUFRegistry {

    private static final Object LOCK = new Object();
    private static final Logger log = LoggerFactory.getLogger(UUFRegistry.class);

    private final AppDiscoverer appDiscoverer;
    private final AppCreator appCreator;
    private Map<String, App> apps;
    private final StaticResolver staticResolver;
    private Debugger debugger;

    public UUFRegistry(AppDiscoverer appDiscoverer, AppCreator appCreator, StaticResolver staticResolver) {
        this.appDiscoverer = appDiscoverer;
        this.appCreator = appCreator;
        this.apps = null;
        this.staticResolver = staticResolver;
    }

    public void serve(HttpRequest request, HttpResponse response) {
        if (log.isDebugEnabled() && !request.isDebugRequest()) {
            log.debug("HTTP request received " + request);
        }

        // We are creating apps on the first request, to provide sufficient time for RenderableCreators to register.
        if (this.apps == null) {
            synchronized (LOCK) {
                if (this.apps == null) {
                    this.apps = loadApps(appDiscoverer, appCreator);
                }
            }
        }

        App app = null;
        try {
            if (!request.isValid()) {
                response.setContent(STATUS_BAD_REQUEST, "Invalid URI '" + request.getUri() + "'.");
                return;
            }
            if (request.isDefaultFaviconRequest()) {
                staticResolver.serveDefaultFavicon(request, response);
                return;
            }

            app = apps.get(request.getAppContext());
            if (app == null) {
                response.setContent(STATUS_NOT_FOUND,
                                    "Cannot find an app for context '" + request.getAppContext() + "'.");
                return;
            }

            if (request.isStaticResourceRequest()) {
                staticResolver.serve(app, request, response);
                return;
            }
            if (Debugger.isDebuggingEnabled()) {
                app = reloadApp(app, appDiscoverer, appCreator);
                if (request.isDebugRequest() && (this.debugger == null)) {
                    synchronized (LOCK) {
                        if (this.debugger == null) {
                            this.debugger = new Debugger(); // Create a debugger.
                        }
                    }
                    debugger.serve(app, request, response);
                    return;
                }
            }
            String html;
            if (request.isFragmentRequest()) {
                html = app.renderFragment(request, response);
            } else {
                // Request for a page.
                try {
                    html = app.renderPage(request, response);
                } catch (PageNotFoundException e) {
                    // See https://googlewebmastercentral.blogspot.com/2010/04/to-slash-or-not-to-slash.html
                    // If the tailing '/' is extra or a it is missing, then send 301 with corrected URL.
                    String uri = request.getUri();
                    String correctedUri = uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri + "/";
                    if (app.hasPage(correctedUri)) {
                        response.setStatus(STATUS_MOVED_PERMANENTLY);
                        response.setHeader(HEADER_LOCATION, request.getHostName() + correctedUri);
                        return;
                    }
                    throw e;
                }
            }
            response.setContent(STATUS_OK, html, CONTENT_TYPE_TEXT_HTML);
        } catch (PageNotFoundException e) {
            renderErrorPage(app, request, response, e);
        } catch (FragmentNotFoundException e) {
            response.setContent(e.getHttpStatusCode(), e.getMessage());
        } catch (PageRedirectException e) {
            response.setStatus(STATUS_FOUND);
            response.setHeader(HEADER_LOCATION, e.getRedirectUrl());
        } catch (HttpErrorException e) {
            renderErrorPage(app, request, response, e);
        } catch (UUFException e) {
            log.error("A server error occurred while serving for request '" + request + "'.", e);
            renderErrorPage(app, request, response,
                            new HttpErrorException(STATUS_INTERNAL_SERVER_ERROR, e.getMessage(), e));
        } catch (Exception e) {
            log.error("An unexpected error occurred while serving for request '" + request + "'.", e);
            renderErrorPage(app, request, response,
                            new HttpErrorException(STATUS_INTERNAL_SERVER_ERROR, e.getMessage(), e));
        }
    }

    private static Map<String, App> loadApps(AppDiscoverer appDiscoverer, AppCreator appCreator) {
        return appDiscoverer.getAppReferences()
                .map(appReference -> {
                    App app = appCreator.createApp(appReference);
                    log.info("App '" + app.getName() + "' created.");
                    return app;
                })
                .collect(Collectors.toMap(App::getContext, app -> app));
    }

    private static App reloadApp(App app, AppDiscoverer appDiscoverer, AppCreator appCreator) {
        String appName = app.getName();
        return appDiscoverer.getAppReferences()
                .filter(appReference -> appName.equals(appReference.getName()))
                .findFirst()
                .map(appCreator::createApp)
                .orElseThrow(() -> new UUFException("Cannot reload app '" + appName + "'."));
    }

    private void renderErrorPage(App app, HttpRequest request, HttpResponse response, HttpErrorException ex) {
        if (app == null) {
            // Exception occurred before creating/retrieving the app. So we cannot render configured error pages.
            response.setContent(ex.getHttpStatusCode(), ex.getMessage());
            return;
        }

        try {
            Optional<String> html = app.renderErrorPage(ex, request, response);
            if (html.isPresent()) {
                response.setContent(STATUS_OK, html.get(), CONTENT_TYPE_TEXT_HTML);
            } else {
                // Error page is not configured.
                response.setContent(ex.getHttpStatusCode(), ex.getMessage());
            }
        } catch (Exception e) {
            // Another exception occurred when rendering the error page for HttpErrorException ex.
            log.error("An error occurred when rendering the error page for exception '" + ex + "'.", e);
            response.setContent(ex.getHttpStatusCode(), ex.getMessage());
        }
    }
}
