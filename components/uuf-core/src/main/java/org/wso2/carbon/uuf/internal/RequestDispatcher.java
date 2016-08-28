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
import org.wso2.carbon.uuf.exception.HttpErrorException;
import org.wso2.carbon.uuf.exception.PageNotFoundException;
import org.wso2.carbon.uuf.exception.PageRedirectException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.debug.Debugger;
import org.wso2.carbon.uuf.internal.io.StaticResolver;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;

import static org.wso2.carbon.uuf.spi.HttpResponse.CONTENT_TYPE_TEXT_HTML;
import static org.wso2.carbon.uuf.spi.HttpResponse.HEADER_LOCATION;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_FOUND;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_MOVED_PERMANENTLY;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_OK;

public class RequestDispatcher {

    private static final Logger log = LoggerFactory.getLogger(RequestDispatcher.class);

    private final StaticResolver staticResolver;
    private final Debugger debugger;

    public RequestDispatcher() {
        this(new StaticResolver(), (Debugger.isDebuggingEnabled() ? new Debugger() : null));
    }

    public RequestDispatcher(StaticResolver staticResolver, Debugger debugger) {
        this.staticResolver = staticResolver;
        this.debugger = debugger;
    }

    public void serve(App app, HttpRequest request, HttpResponse response) {
        if (log.isDebugEnabled() && !request.isDebugRequest()) {
            log.debug("HTTP request received " + request);
        }

        try {
            if (request.isStaticResourceRequest()) {
                staticResolver.serve(app, request, response);
            } else if (Debugger.isDebuggingEnabled() && request.isDebugRequest()) {
                debugger.serve(app, request, response);
            } else {
                servePageOrFragment(app, request, response);
            }
        } catch (PageRedirectException e) {
            response.setStatus(STATUS_FOUND);
            response.setHeader(HEADER_LOCATION, e.getRedirectUrl());
        } catch (HttpErrorException e) {
            serveDefaultErrorPage(e.getHttpStatusCode(), e.getMessage(), response);
        } catch (UUFException e) {
            String msg = "A server error occurred while serving for request '" + request + "'.";
            log.error(msg, e);
            serveDefaultErrorPage(STATUS_INTERNAL_SERVER_ERROR, msg, response);
        } catch (Exception e) {
            String msg = "An unexpected error occurred while serving for request '" + request + "'.";
            log.error(msg, e);
            serveDefaultErrorPage(STATUS_INTERNAL_SERVER_ERROR, msg, response);
        }
    }

    private void servePageOrFragment(App app, HttpRequest request, HttpResponse response) {
        try {
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
                    String uriWithoutContextPath = request.getUriWithoutContextPath();
                    String correctedUriWithoutContextPath = uriWithoutContextPath.endsWith("/") ?
                            uriWithoutContextPath.substring(0, uriWithoutContextPath.length() - 1) :
                            (uriWithoutContextPath + "/");
                    if (app.hasPage(correctedUriWithoutContextPath)) {
                        response.setStatus(STATUS_MOVED_PERMANENTLY);
                        String correctedUri = request.getContextPath() + correctedUriWithoutContextPath;
                        if (request.getQueryString() != null) {
                            correctedUri = correctedUri + '?' + request.getQueryString();
                        }
                        response.setHeader(HEADER_LOCATION, correctedUri);
                        return;
                    }
                    throw e;
                }
            }
            response.setContent(STATUS_OK, html, CONTENT_TYPE_TEXT_HTML);
        } catch (UUFException e) {
            throw e;
        } catch (Exception e) {
            // May be an UUFException cause this 'e' Exception. Let's unwrap 'e' and find out.
            Throwable th = e;
            while ((th = th.getCause()) != null) {
                if (th instanceof UUFException) {
                    // Cause of 'e' is an UUFException. Throw 'th' so that we can handle it properly.
                    throw (UUFException) th;
                }
            }
            // Cause of 'e' is not an UUFException.
            throw e;
        }
    }

    public void serveDefaultErrorPage(int httpStatusCode, String content, HttpResponse response) {
        response.setContent(httpStatusCode, content);
    }

    public void serveDefaultFavicon(HttpRequest request, HttpResponse response) {
        staticResolver.serveDefaultFavicon(request, response);
    }
}
