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
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.exception.FragmentNotFoundException;
import org.wso2.carbon.uuf.exception.HttpErrorException;
import org.wso2.carbon.uuf.exception.PageNotFoundException;
import org.wso2.carbon.uuf.exception.PageRedirectException;
import org.wso2.carbon.uuf.exception.UnauthorizedException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.debug.Debugger;
import org.wso2.carbon.uuf.internal.io.StaticResolver;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;

import java.util.Optional;

import static org.wso2.carbon.uuf.spi.HttpResponse.CONTENT_TYPE_TEXT_HTML;
import static org.wso2.carbon.uuf.spi.HttpResponse.HEADER_LOCATION;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_FOUND;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_MOVED_PERMANENTLY;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_OK;

public class RequestDispatcher {

    private static final Logger log = LoggerFactory.getLogger(RequestDispatcher.class);

    private final StaticResolver staticResolver = new StaticResolver();
    private final Object lock = new Object();
    private Debugger debugger;

    public void serve(App app, HttpRequest request, HttpResponse response) {
        if (log.isDebugEnabled() && !request.isDebugRequest()) {
            log.debug("HTTP request received " + request);
        }

        try {
            if (request.isStaticResourceRequest()) {
                staticResolver.serve(app, request, response);
                return;
            }
            if (Debugger.isDebuggingEnabled() && request.isDebugRequest()) {
                if (this.debugger == null) {
                    synchronized (lock) {
                        if (this.debugger == null) {
                            this.debugger = new Debugger(); // Create a debugger.
                        }
                    }
                }
                debugger.serve(app, request, response);
                return;
            }
            serveApp(app, request, response);
        } catch (PageNotFoundException | UnauthorizedException e) {
            serveErrorPage(app, request, response, e);
        } catch (FragmentNotFoundException e) {
            response.setContent(e.getHttpStatusCode(), e.getMessage());
        } catch (PageRedirectException e) {
            response.setStatus(STATUS_FOUND);
            response.setHeader(HEADER_LOCATION, e.getRedirectUrl());
        } catch (HttpErrorException e) {
            serveErrorPage(app, request, response, e);
        } catch (UUFException e) {
            log.error("A server error occurred while serving for request '" + request + "'.", e);
            serveErrorPage(app, request, response,
                           new HttpErrorException(STATUS_INTERNAL_SERVER_ERROR, e.getMessage(), e));
        } catch (Exception e) {
            log.error("An unexpected error occurred while serving for request '" + request + "'.", e);
            serveErrorPage(app, request, response,
                           new HttpErrorException(STATUS_INTERNAL_SERVER_ERROR, e.getMessage(), e));
        }
    }

    public void serveDefaultFavicon(HttpRequest request, HttpResponse response) {
        staticResolver.serveDefaultFavicon(request, response);
    }

    public void serveErrorPage(HttpRequest request, HttpResponse response, int httpStatus, String message) {
        serveErrorPage(null, request, response, new HttpErrorException(httpStatus, message));
    }

    public void serveErrorPage(App app, HttpRequest request, HttpResponse response, HttpErrorException ex) {
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

    private void serveApp(App app, HttpRequest request, HttpResponse response){
        String html;
        try {
            if (request.isFragmentRequest()) {
                html = app.renderFragment(request, response);
            } else {
                // Request for a page.
                try {
                    html = app.renderPage(request, response);
                } catch (PageNotFoundException e) {
                    // See https://googlewebmastercentral.blogspot.com/2010/04/to-slash-or-not-to-slash.html
                    // If the tailing '/' is extra or a it is missing, then send 301 with corrected URL.
                    String uriWithoutContextPath = request.getUriWithoutContextPath();
                    String correctedUriWithoutContextPath = uriWithoutContextPath.endsWith("/") ?
                            uriWithoutContextPath.substring(0, uriWithoutContextPath.length() - 1) :
                            (uriWithoutContextPath + "/");
                    if (app.hasPage(correctedUriWithoutContextPath)) {
                        response.setStatus(STATUS_MOVED_PERMANENTLY);
                        String correctedUrl =
                                request.getHostName() + request.getContextPath() + correctedUriWithoutContextPath;
                        response.setHeader(HEADER_LOCATION, correctedUrl);
                        return;
                    }
                    throw e;
                }
            }
            response.setContent(STATUS_OK, html, CONTENT_TYPE_TEXT_HTML);
        } catch (UUFException e){
            throw e;
        } catch (Exception e) {
            // this is to unwrap hadlebarsException and throw UUFException(Eg: UnauthorizedException from helper)
            Throwable th;
            while ((th = e.getCause()) != null) {
                if (th instanceof UUFException) {
                    throw (UUFException) th;
                }
            }
            throw e;
        }
    }
}
