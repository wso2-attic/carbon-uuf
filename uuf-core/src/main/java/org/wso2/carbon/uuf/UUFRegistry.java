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

package org.wso2.carbon.uuf;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.MimeMapper;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.core.create.AppCreator;
import org.wso2.carbon.uuf.core.create.AppResolver;
import org.wso2.carbon.uuf.core.exception.FragmentNotFoundException;
import org.wso2.carbon.uuf.core.exception.PageNotFoundException;
import org.wso2.carbon.uuf.core.exception.PageRedirectException;
import org.wso2.carbon.uuf.core.exception.UUFException;
import org.wso2.carbon.uuf.fileio.HttpRequest;
import org.wso2.carbon.uuf.fileio.StaticResolver;
import org.wso2.msf4j.util.SystemVariableUtil;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UUFRegistry {

    private static final Logger log = LoggerFactory.getLogger(UUFRegistry.class);

    private final AppCreator appCreator;
    private final Optional<DebugAppender> debugAppender;
    private final Map<String, App> apps = new HashMap<>();
    private final StaticResolver staticResolver;
    private AppResolver appResolver;
    public static final String FRAGMENTS_URI_PREFIX = "/fragments/";

    public UUFRegistry(AppCreator appCreator, Optional<DebugAppender> debugAppender, AppResolver appResolver,
                       StaticResolver staticResolver) {
        this.appCreator = appCreator;
        this.debugAppender = debugAppender;
        this.appResolver = appResolver;
        this.staticResolver = staticResolver;
    }

    public static Optional<DebugAppender> createDebugAppender() {
        String uufDebug = SystemVariableUtil.getValue("uufDebug", "false");
        if (uufDebug.equalsIgnoreCase("true")) {
            DebugAppender appender = new DebugAppender();
            appender.attach();
            return Optional.of(appender);
        } else {
            return Optional.empty();
        }
    }

    public Response.ResponseBuilder serve(HttpRequest request) {
        String hostHeader = request.getHeaders().get(HttpHeaders.HOST);
        String host = "//" + ((hostHeader == null) ? "localhost" : hostHeader);
        String uri = request.getRequestURI().replaceAll("/+", "/");
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }

        int firstSlash = uri.indexOf('/', 1);

        if (firstSlash < 0) {
            if (uri.equals("/favicon.ico")) {
                //TODO: send a favicon, cacheable favicon avoids frequent requests for it.
                return Response.status(404).entity("");
            }

            // eg: url = http://example.com/app and uri = /app
            // since we don't support ROOT app, this must be a mis-type
            return Response.status(301).entity("").header(HttpHeaders.LOCATION, uri + "/");
        }

        String appName = uri.substring(1, firstSlash);
        String appContext = uri.substring(0, firstSlash);
        String uriWithoutAppContext = uri.substring(firstSlash, uri.length());

        if (log.isDebugEnabled() && !uriWithoutAppContext.startsWith("/debug/")) {
            log.debug("request received " + request.getMethod() + " " + request.getRequestURI() + " " +
                    request.getProtocol());
        }

        App app = apps.get(appName);
        try {
            if (StaticResolver.isStaticResourceUri(uriWithoutAppContext)) {
                // App class is unaware of static path resolving. Hence static file serving can easily ported into a
                // separate server.
                return staticResolver.createResponse(appName, uriWithoutAppContext, request);
            } else {
                if (app == null || debugAppender.isPresent()) {
                    app = appCreator.createApp(appContext, appResolver.resolve(appName));
                    apps.put(appName, app);
                }
                if (uriWithoutAppContext.startsWith("/debug/")) {
                    return renderDebug(app, uriWithoutAppContext);
                } else if (isFragmentsUri(uriWithoutAppContext)) {
                    RequestLookup requestLookup = new RequestLookup(appContext, request);
                    String fragmentResult = app.renderFragment(uri.substring(appContext.length()),
                            new RequestLookup(appContext, request));
                    Response.ResponseBuilder responseBuilder = ifExistsAddResponseHeaders(Response.ok(fragmentResult),
                            requestLookup
                                    .getResponseHeaders());
                    return responseBuilder.header(HttpHeaders.CONTENT_TYPE, "text/html");
                } else {
                    RequestLookup requestLookup = new RequestLookup(appContext, request);
                    String pageResult = app.renderPage(uri.substring(appContext.length()), requestLookup);
                    Response.ResponseBuilder responseBuilder = ifExistsAddResponseHeaders(Response.ok(pageResult),
                            requestLookup
                                    .getResponseHeaders());
                    return responseBuilder.header(HttpHeaders.CONTENT_TYPE, "text/html");
                }
            }
        } catch (PageNotFoundException | FragmentNotFoundException e) {
            // https://googlewebmastercentral.blogspot.com/2010/04/to-slash-or-not-to-slash.html
            // if the tailing / is extra or a it is missing, send 301
            if (app != null) {
                if (uri.endsWith("/")) {
                    String uriWithoutSlash = uriWithoutAppContext.substring(0, uriWithoutAppContext.length() - 1);
                    if (app.hasPage(uriWithoutSlash)) {
                        return Response.status(301).header(HttpHeaders.LOCATION, host + uriWithoutSlash);
                    }
                } else {
                    String uriWithSlash = uriWithoutAppContext + "/";
                    if (app.hasPage(uriWithSlash)) {
                        return Response.status(301).header(HttpHeaders.LOCATION, host + uri + "/");
                    }
                }
            }
            return createErrorResponse(appName, e.getMessage(), e, e.getHttpStatusCode());
        } catch (PageRedirectException e) {
            return createErrorResponse(appName, e.getMessage(), e, e.getHttpStatusCode()).header("Location", e.getRedirectUrl());
        } catch (Exception e) {
            int httpStatusCode = 500;
            Throwable cause = e.getCause();
            //TODO check this loop's logic
            while (cause != null) {
                if (cause instanceof PageNotFoundException) {
                    httpStatusCode = ((PageNotFoundException) cause).getHttpStatusCode();
                    break;
                }
                if (cause instanceof UUFException) {
                    break;
                }
                if (cause == cause.getCause()) {
                    break;
                }
                cause = cause.getCause();
            }
            return createErrorResponse(appName, e, httpStatusCode);
        }
    }

    private Response.ResponseBuilder renderDebug(App app, String resourcePath) {
        if (resourcePath.equals("/debug/api/pages/")) {
            //TODO: fix issues when same page is in multiple components
            return Response.ok(app.getComponents().entrySet().stream()
                    .flatMap(entry -> entry.getValue().getPages().stream())
                    .collect(Collectors.toSet()));
        }
        if (resourcePath.startsWith("/debug/api/fragments/")) {
            return Response.ok(app.getComponents().entrySet().stream()
                    .flatMap(entry -> entry.getValue().getFragments().values().stream())
                    .collect(Collectors.toSet()));
        }
        if (resourcePath.startsWith("/debug/logs")) {
            if (debugAppender.isPresent()) {
                return Response.ok(debugAppender.get().asJson(), "application/json");
            } else {
                return Response.status(Response.Status.GONE);
            }
        }
        if (resourcePath.startsWith("/debug/")) {
            if (resourcePath.endsWith("/")) {
                resourcePath = resourcePath + "index.html";
            }
            InputStream resourceAsStream = this.getClass().getResourceAsStream("/apps" + resourcePath);
            if (resourceAsStream == null) {
                return Response.status(Response.Status.NOT_FOUND);
            }
            try {
                String debugContent = IOUtils.toString(resourceAsStream, "UTF-8");
                return Response.ok(debugContent, getMime(resourcePath));
            } catch (IOException e) {
                return Response.serverError().entity(e.getMessage());
            }
        }
        throw new UUFException("Unknown debug request");
    }

    public static boolean isFragmentsUri(String uriWithoutContext) {
        return uriWithoutContext.startsWith(FRAGMENTS_URI_PREFIX);
    }

    private String getMime(String resourcePath) {
        int extensionIndex = resourcePath.lastIndexOf(".");
        String extension = (extensionIndex == -1) ? resourcePath : resourcePath.substring(extensionIndex + 1,
                resourcePath.length());
        Optional<String> mime = MimeMapper.getMimeType(extension);
        return (mime.isPresent()) ? mime.get() : "text/html";
    }

    private Response.ResponseBuilder createErrorResponse(String appName, Exception e, int httpStatusCode) {
        String errorMessage = "Error while serving context /'" + appName + "'.";
        return createErrorResponse(appName, errorMessage, e, httpStatusCode);
    }

    private Response.ResponseBuilder createErrorResponse(String appName, String errorMessage, Exception e, int httpStatusCode) {
        log.error(errorMessage, e);
        return Response.status(httpStatusCode).entity(errorMessage).header(HttpHeaders.CONTENT_TYPE, "text/plain");
    }

    private Response.ResponseBuilder ifExistsAddResponseHeaders(Response.ResponseBuilder responseBuilder,
                                                                Map<String, String> headers) {
        headers.entrySet().stream().forEach(
                entry -> responseBuilder.header(entry.getKey(), entry.getValue()));
        return responseBuilder;
    }
}
