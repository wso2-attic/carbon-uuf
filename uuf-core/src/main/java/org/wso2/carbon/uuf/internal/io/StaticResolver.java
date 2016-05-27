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

package org.wso2.carbon.uuf.internal.io;

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.uuf.api.HttpRequest;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.internal.util.MimeMapper;
import org.wso2.carbon.uuf.internal.util.RequestUtil;
import org.wso2.carbon.uuf.reference.ComponentReference;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import static org.wso2.carbon.uuf.reference.AppReference.DIR_NAME_COMPONENTS;
import static org.wso2.carbon.uuf.reference.AppReference.DIR_NAME_THEMES;

public class StaticResolver {

    public static final String DIR_NAME_COMPONENT_RESOURCES = "base";
    public static final String DIR_NAME_PUBLIC_RESOURCES = "public";
    private static final String CACHE_HEADER_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private final Path appsHome;

    /**
     * This constructor will assume uufHome as $PRODUCT_HOME/deployment/uufapps
     */
    public StaticResolver() {
        this(Utils.getCarbonHome().resolve("deployment").resolve("uufapps"));
    }

    public StaticResolver(Path appsHome) {
        this.appsHome = appsHome.normalize();
    }

    public Response.ResponseBuilder createDefaultFaviconResponse(HttpRequest request) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("/favicon.png");
        try {
            // Since default favicon is very small (~1.9 kB) it is ok to load it directly to the memory.
            byte[] data = IOUtils.toByteArray(inputStream);
            // FIXME: 5/15/16 MSF4J doesn't support InputStream or byte arrays
            return Response.ok().entity(data).type("image/png");
        } catch (IOException e) {
            // This never happens.
            return Response.serverError().entity("Cannot read default favicon.");
        }
    }

    public Response.ResponseBuilder createResponse(App app, HttpRequest request) {
        Path resourcePath;
        try {
            if (RequestUtil.isComponentStaticResourceRequest(request)) {
                // /public/components/...
                resourcePath = resolveResourceInComponent(app.getName(), request.getUriWithoutAppContext());
            } else if (RequestUtil.isThemeStaticResourceRequest(request)) {
                // /public/themes/...
                resourcePath = resolveResourceInTheme(app.getName(), request.getUriWithoutAppContext());
            } else {
                // /public/...
                return Response.status(400)
                        .entity("Invalid static resource URI '" + request.getUri() + "'.")
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain");
            }
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(e.getMessage()).header(HttpHeaders.CONTENT_TYPE, "text/plain");
        } catch (Exception e) {
            // IOException or any other Exception
            return Response.serverError()
                    .entity("A server occurred while serving for static resource request '" + request.getUri() + "'.");
        }

        if (Files.isRegularFile(resourcePath) && !Files.isDirectory(resourcePath)) {
            // This is an existing regular, non-directory file.
            return getResponseBuilder(resourcePath, request);
        } else {
            // Either file does not exists or it is a non-regular file. i.e. a directory
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Requested resource '" + request.getUri() + "' does not exists.");
        }
    }

    private Path resolveResourceInComponent(String appName, String uriWithoutAppContext) {
        // Correct 'uriWithoutAppContext' value must be in either
        // "/public/components/{component-simple-name}/{fragment-simple-name}/{sub-directory}/{rest-of-the-path}"
        // format or in
        // "/public/components/{component-simple-name}/base/{sub-directory}/{rest-of-the-path}" format.
        // So there should be at least 6 slashes. Don't worry about multiple consecutive slashes. They  are covered
        // in RequestUtil.isValid(HttpRequest) method which is called before this method.

        int slashesCount = 0, thirdSlashIndex = -1, fourthSlashIndex = -1, fifthSlashIndex = -1;
        for (int i = 0; i < uriWithoutAppContext.length(); i++) {
            if (uriWithoutAppContext.charAt(i) == '/') {
                slashesCount++;
                if (slashesCount == 3) {
                    thirdSlashIndex = i;
                } else if (slashesCount == 4) {
                    fourthSlashIndex = i;
                } else if (slashesCount == 5) {
                    fifthSlashIndex = i;
                } else if (slashesCount == 6) {
                    break;
                }
            }
        }
        if (slashesCount != 6) {
            throw new IllegalArgumentException("Invalid static resource URI '" + uriWithoutAppContext + "'.");
        }

        Path staticFilePath = appsHome.resolve(appName).resolve(DIR_NAME_COMPONENTS);
        String componentSimpleName = uriWithoutAppContext.substring(thirdSlashIndex + 1, fourthSlashIndex);
        staticFilePath = staticFilePath.resolve(componentSimpleName);
        String fragmentSimpleName = uriWithoutAppContext.substring(fourthSlashIndex + 1, fifthSlashIndex);
        if (fragmentSimpleName.equals(DIR_NAME_COMPONENT_RESOURCES)) {
            staticFilePath = staticFilePath.resolve(DIR_NAME_PUBLIC_RESOURCES);
        } else {
            staticFilePath = staticFilePath.resolve(ComponentReference.DIR_NAME_FRAGMENTS)
                    .resolve(fragmentSimpleName)
                    .resolve(DIR_NAME_PUBLIC_RESOURCES);
        }
        // {sub-directory}/{rest-of-the-path}
        String relativePathString = uriWithoutAppContext.substring(fifthSlashIndex + 1, uriWithoutAppContext.length());
        return staticFilePath.resolve(relativePathString);
    }

    private Path resolveResourceInTheme(String appName, String uriWithoutAppContext) {
        // Correct 'uriWithoutAppContext' value must be in
        // "/public/themes/{theme-name}/{sub-directory}/{rest-of-the-path}" format.
        // So there should be at least 5 slashes. Don't worry about multiple consecutive slashes. They  are covered
        // in RequestUtil.isValid(HttpRequest) method which is called before this method.

        int slashesCount = 0, thirdSlashIndex = -1, fourthSlashIndex = -1;
        for (int i = 0; i < uriWithoutAppContext.length(); i++) {
            if (uriWithoutAppContext.charAt(i) == '/') {
                slashesCount++;
                if (slashesCount == 3) {
                    thirdSlashIndex = i;
                } else if (slashesCount == 4) {
                    fourthSlashIndex = i;
                } else if (slashesCount == 5) {
                    break;
                }
            }
        }
        if (slashesCount != 5) {
            throw new IllegalArgumentException("Invalid static resource URI '" + uriWithoutAppContext + "'.");
        }

        String themeSimpleName = uriWithoutAppContext.substring(thirdSlashIndex + 1, fourthSlashIndex);
        // {sub-directory}/{rest-of-the-path}
        String relativePathString = uriWithoutAppContext.substring(fourthSlashIndex + 1, uriWithoutAppContext.length());
        return appsHome.resolve(appName).resolve(DIR_NAME_THEMES)
                .resolve(themeSimpleName).resolve(DIR_NAME_PUBLIC_RESOURCES).resolve(relativePathString);

    }

    private Response.ResponseBuilder getResponseBuilder(Path resource, HttpRequest request) {
        try {
            Optional<Date> ifModDate = getIfModifiedSinceDate(request);
            BasicFileAttributes attrs = Files.readAttributes(resource, BasicFileAttributes.class);
            Date resourceModDate = new Date(attrs.lastModifiedTime().toMillis());
            if (ifModDate.isPresent() && (!ifModDate.get().after(resourceModDate))) {//!after = before || equal
                return Response.notModified();
            }
            return setCacheHeaders(resourceModDate, Response.ok(resource.toFile(), getMime(resource.toString())));
        } catch (IOException e) {
            return Response.serverError().entity(e.getMessage());
        }
    }

    private Optional<Date> getIfModifiedSinceDate(HttpRequest request) {
        String httpDateStr = request.getHeaders().get("If-Modified-Since");
        if (httpDateStr == null) {
            return Optional.empty();
        }
        SimpleDateFormat df = new SimpleDateFormat(CACHE_HEADER_DATE_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            return Optional.of(df.parse(httpDateStr));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    private String getMime(String resourcePath) {
        int extensionIndex = resourcePath.lastIndexOf(".");
        String extension = (extensionIndex == -1) ? resourcePath : resourcePath.substring(extensionIndex + 1,
                                                                                          resourcePath.length());
        Optional<String> mime = MimeMapper.getMimeType(extension);
        return (mime.isPresent()) ? mime.get() : "text/html";
    }

    private Response.ResponseBuilder setCacheHeaders(Date lastModDate, Response.ResponseBuilder builder) {
        // Currently MSF4J does not implement cacheControl.Hence cache control headers are set manually
        SimpleDateFormat df = new SimpleDateFormat(CACHE_HEADER_DATE_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        builder.header("Last-Modified", df.format(lastModDate));
        builder.header("Cache-Control", "public,max-age=2592000");
        return builder;
    }
}