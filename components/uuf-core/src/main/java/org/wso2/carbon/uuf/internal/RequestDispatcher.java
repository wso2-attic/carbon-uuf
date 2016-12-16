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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.exception.HttpErrorException;
import org.wso2.carbon.uuf.exception.PageRedirectException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.debug.Debugger;
import org.wso2.carbon.uuf.internal.io.StaticResolver;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;

import static org.wso2.carbon.uuf.spi.HttpResponse.CONTENT_TYPE_TEXT_HTML;
import static org.wso2.carbon.uuf.spi.HttpResponse.HEADER_CACHE_CONTROL;
import static org.wso2.carbon.uuf.spi.HttpResponse.HEADER_EXPIRES;
import static org.wso2.carbon.uuf.spi.HttpResponse.HEADER_LOCATION;
import static org.wso2.carbon.uuf.spi.HttpResponse.HEADER_PRAGMA;
import static org.wso2.carbon.uuf.spi.HttpResponse.HEADER_X_CONTENT_TYPE_OPTIONS;
import static org.wso2.carbon.uuf.spi.HttpResponse.HEADER_X_XSS_PROTECTION;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_FOUND;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_OK;

public class RequestDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDispatcher.class);

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
            LOGGER.error(msg, e);
            serveDefaultErrorPage(STATUS_INTERNAL_SERVER_ERROR, msg, response);
        } catch (Exception e) {
            String msg = "An unexpected error occurred while serving for request '" + request + "'.";
            LOGGER.error(msg, e);
            serveDefaultErrorPage(STATUS_INTERNAL_SERVER_ERROR, msg, response);
        }
    }

    private void servePageOrFragment(App app, HttpRequest request, HttpResponse response) {
        try {
            String html;
            // set default mandatory http headers for security purpose
            setDefaultSecurityHeaders(response);
            if (request.isFragmentRequest()) {
                html = app.renderFragment(request, response);
            } else {
                // Request for a page.
                html = app.renderPage(request, response);
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

    /**
     * Sets some default and mandatory security related headers to the response path.
     *
     * @param httpResponse the http response instance used with setting the headers.
     */
    private void setDefaultSecurityHeaders(HttpResponse httpResponse) {
        httpResponse.setHeader(HEADER_X_CONTENT_TYPE_OPTIONS, "nosniff");
        httpResponse.setHeader(HEADER_X_XSS_PROTECTION, "1; mode=block");
        httpResponse.setHeader(HEADER_CACHE_CONTROL, "no-store, no-cache, must-revalidate, private");
        httpResponse.setHeader(HEADER_EXPIRES, "0");
        httpResponse.setHeader(HEADER_PRAGMA, "no-cache");
    }
}
