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

package org.wso2.carbon.uuf.fileio;

import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.uuf.core.MimeMapper;
import org.wso2.carbon.uuf.core.create.AppReference;
import org.wso2.carbon.uuf.core.create.ComponentReference;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

public class StaticResolver {

    private static final String DIR_NAME_COMPONENT_RESOURCES = "base";
    private static final String DIR_NAME_FRAGMENT_RESOURCES = "public";
    private static final String CACHE_HEADER_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private final Path uufHome;

    /**
     * This constructor will assume uufHome as $PRODUCT_HOME/deployment/uufapps
     */
    public StaticResolver() {
        this(Utils.getCarbonHome().resolve("deployment").resolve("uufapps"));
    }

    public StaticResolver(Path uufHome) {
        this.uufHome = uufHome;
    }

    /**
     * This method creates a Response upon static resource requests uris. Possible URI types are;
     * <ol>
     * <li>Component Static Resource URI, syntax: /public/{componentName}/base/{subResourceUri}</li>
     * <li>Fragment Static Resource URI, syntax: /public/{componentName}/{fragmentName}/{subResourceUri}</li>
     * </ol>
     * These URI types are mapped into following file path syntax on the file system;
     * <ul>
     * <li>$UUF_HOME/{appName}/components/{componentName}/[{fragmentName}|base]/public/{subResourcePath}</li>
     * </ul>
     *
     * @param appName Application Name
     * @param uri     Static Resource Uri
     * @param request Static Resource Request
     * @return Response Builder
     */
    public Response.ResponseBuilder createResponse(String appName, String uri, HttpRequest request) {
        Path resource = resolveUri(appName, uri);
        if (Files.exists(resource) && Files.isRegularFile(resource)) {
            return getResponseBuilder(resource, request);
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    "Requested resource '" + uri + "' does not exists at '/" + uufHome.relativize(resource) + "'.");
        }
    }

    private Path resolveUri(String appName, String uri) {
        String resourcePathParts[] = uri.split("/");
        if (resourcePathParts.length < 5) {
            throw new IllegalArgumentException("Resource URI '" + uri + "' is invalid.");
        }

        String componentName = resourcePathParts[2];
        String fragmentName = resourcePathParts[3];
        int fourthSlash = StringUtils.ordinalIndexOf(uri, "/", 4);
        String subResourcePath = uri.substring(fourthSlash + 1, uri.length());
        Path componentPath = uufHome.resolve(appName).resolve(AppReference.DIR_NAME_COMPONENTS).resolve(componentName);
        Path fragmentPath;
        if (fragmentName.equals(DIR_NAME_COMPONENT_RESOURCES)) {
            fragmentPath = componentPath;
        } else {
            fragmentPath = componentPath.resolve(ComponentReference.DIR_NAME_FRAGMENTS).resolve(fragmentName);
        }
        return fragmentPath.resolve(DIR_NAME_FRAGMENT_RESOURCES).resolve(subResourcePath);
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
        String httpDateStr = request.headers().get("If-Modified-Since");
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