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

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.uuf.api.HttpRequest;
import org.wso2.carbon.uuf.api.HttpResponse;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.internal.util.MimeMapper;
import org.wso2.carbon.uuf.reference.ComponentReference;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static org.wso2.carbon.uuf.api.HttpResponse.CONTENT_TYPE_IMAGE_PNG;
import static org.wso2.carbon.uuf.api.HttpResponse.CONTENT_TYPE_WILDCARD;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_BAD_REQUEST;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_NOT_FOUND;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_NOT_MODIFIED;
import static org.wso2.carbon.uuf.api.HttpResponse.STATUS_OK;
import static org.wso2.carbon.uuf.reference.AppReference.DIR_NAME_COMPONENTS;
import static org.wso2.carbon.uuf.reference.AppReference.DIR_NAME_THEMES;

public class StaticResolver {

    public static final String DIR_NAME_COMPONENT_RESOURCES = "base";
    public static final String DIR_NAME_PUBLIC_RESOURCES = "public";

    private static final DateTimeFormatter HTTP_DATE_FORMATTER;
    private static final ZoneId GMT_TIME_ZONE;
    private static final Logger log = LoggerFactory.getLogger(StaticResolver.class);

    private final Path appsHome;

    static {
        // See https://tools.ietf.org/html/rfc7231#section-7.1.1.1
        HTTP_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz");
        GMT_TIME_ZONE = ZoneId.of("GMT");
    }

    /**
     * This constructor will assume uufHome as $PRODUCT_HOME/deployment/uufapps
     */
    public StaticResolver() {
        this(Utils.getCarbonHome().resolve("deployment").resolve("uufapps"));
    }

    public StaticResolver(Path appsHome) {
        this.appsHome = appsHome.normalize();
    }

    public void serveDefaultFavicon(HttpRequest request, HttpResponse response) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("/favicon.png");
        if (inputStream == null) {
            log.error("Cannot find default favicon 'favicon.png' in classpath.");
            response.setStatus(STATUS_NOT_FOUND);
        } else {
            response.setStatus(STATUS_OK);
            response.setContent(inputStream, CONTENT_TYPE_IMAGE_PNG);
        }
    }

    public void serve(App app, HttpRequest request, HttpResponse response) {
        Path resourcePath;
        try {
            if (request.isComponentStaticResourceRequest()) {
                // /public/components/...
                resourcePath = resolveResourceInComponent(app.getName(), request.getUriWithoutAppContext());
            } else if (request.isThemeStaticResourceRequest()) {
                // /public/themes/...
                resourcePath = resolveResourceInTheme(app.getName(), request.getUriWithoutAppContext());
            } else {
                // /public/...
                response.setContent(STATUS_BAD_REQUEST, "Invalid static resource URI '" + request.getUri() + "'.");
                return;
            }
        } catch (IllegalArgumentException e) {
            response.setContent(STATUS_BAD_REQUEST, e.getMessage());
            return;
        } catch (Exception e) {
            // IOException or any other Exception
            log.error("An error occurred when manipulating paths for request '" + request + "'.", e);
            response.setContent(STATUS_INTERNAL_SERVER_ERROR,
                                "A server occurred while serving for static resource request '" + request + "'.");
            return;
        }
        if (!Files.isRegularFile(resourcePath) || Files.isDirectory(resourcePath)) {
            // Either file does not exists or it is a non-regular file. i.e. a directory
            response.setContent(STATUS_NOT_FOUND, "Requested resource '" + request.getUri() + "' does not exists.");
            return;
        }

        Optional<ZonedDateTime> modifiedSinceDate = getIfModifiedSinceDate(request);
        ZonedDateTime latModifiedDate;
        try {
            BasicFileAttributes fileAttributes = Files.readAttributes(resourcePath, BasicFileAttributes.class);
            latModifiedDate = ZonedDateTime.ofInstant(fileAttributes.lastModifiedTime().toInstant(), GMT_TIME_ZONE);
        } catch (IOException e) {
            log.error("Cannot read attributes from file '" + resourcePath + "'", e);
            // Since we failed to read file attributes, we cannot set cache headers. So just serve the file
            // without any cache headers.
            response.setStatus(STATUS_OK);
            response.setContent(resourcePath, getContentType(request, resourcePath));
            return;
        }
        if (modifiedSinceDate.isPresent() && Duration.between(modifiedSinceDate.get(), latModifiedDate).isZero()) {
            // Resource is NOT modified since the last serve.
            response.setStatus(STATUS_NOT_MODIFIED);
            return;
        }

        setCacheHeaders(response, latModifiedDate);
        response.setStatus(STATUS_OK);
        response.setContent(resourcePath, getContentType(request, resourcePath));
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

    private Optional<ZonedDateTime> getIfModifiedSinceDate(HttpRequest request) {
        // If-Modified-Since: Sat, 29 Oct 1994 19:43:31 GMT
        String ifModifiedSinceHeader = request.getHeaders().get("If-Modified-Since");
        if (ifModifiedSinceHeader == null) {
            return Optional.<ZonedDateTime>empty();
        }
        try {
            return Optional.of(ZonedDateTime.parse(ifModifiedSinceHeader, HTTP_DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            log.error("Cannot parse 'If-Modified-Since' HTTP header value '" + ifModifiedSinceHeader + "'.", e);
            return Optional.<ZonedDateTime>empty();
        }
    }

    private String getContentType(HttpRequest request, Path resource) {
        String extensionFromUri = FilenameUtils.getExtension(request.getUriWithoutAppContext());
        Optional<String> contentType = MimeMapper.getMimeType(extensionFromUri);
        if (contentType.isPresent()) {
            return contentType.get();
        }
        String extensionFromPath = FilenameUtils.getExtension(resource.getFileName().toString());
        return MimeMapper.getMimeType(extensionFromPath).orElse(CONTENT_TYPE_WILDCARD);
    }

    private void setCacheHeaders(HttpResponse response, ZonedDateTime latModifiedDate) {
        response.setHeader("Last-Modified", HTTP_DATE_FORMATTER.format(latModifiedDate));
        response.setHeader("Cache-Control", "public,max-age=2592000");
    }
}