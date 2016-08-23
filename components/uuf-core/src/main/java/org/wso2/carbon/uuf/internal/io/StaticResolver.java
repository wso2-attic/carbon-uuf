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
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.Theme;
import org.wso2.carbon.uuf.exception.FileOperationException;
import org.wso2.carbon.uuf.exception.ResourceNotFoundException;
import org.wso2.carbon.uuf.internal.UUFServer;
import org.wso2.carbon.uuf.internal.io.util.MimeMapper;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.carbon.uuf.reference.ComponentReference.DIR_NAME_FRAGMENTS;
import static org.wso2.carbon.uuf.spi.HttpResponse.CONTENT_TYPE_IMAGE_PNG;
import static org.wso2.carbon.uuf.spi.HttpResponse.CONTENT_TYPE_WILDCARD;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_BAD_REQUEST;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_NOT_FOUND;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_NOT_MODIFIED;
import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_OK;

public class StaticResolver {

    public static final String DIR_NAME_COMPONENT_RESOURCES = "base";
    public static final String DIR_NAME_PUBLIC_RESOURCES = "public";
    private static final DateTimeFormatter HTTP_DATE_FORMATTER;
    private static final ZoneId GMT_TIME_ZONE;
    private static final Logger log = LoggerFactory.getLogger(StaticResolver.class);

    private final Map<Path, ZonedDateTime> resourcesLastModifiedDates;

    static {
        // See https://tools.ietf.org/html/rfc7231#section-7.1.1.1
        HTTP_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz");
        GMT_TIME_ZONE = ZoneId.of("GMT");
    }

    /**
     * The constructor of StaticResolver class
     */
    public StaticResolver() {
        if (UUFServer.isDevModeEnabled()) {
            /*
             * When the dev mode is enabled, we do not cache last modified dates of serving static resources. This is
             * achieved by setting a dummy map to the 'resourcesLastModifiedDates' field. Dummy map does not store any
             * values and it size is always zero.
             */
            this.resourcesLastModifiedDates = new AbstractMap<Path, ZonedDateTime>() {
                @Override
                public Set<Entry<Path, ZonedDateTime>> entrySet() {
                    return Collections.emptySet(); // No entries in this dummy map.
                }

                @Override
                public ZonedDateTime put(Path key, ZonedDateTime value) {
                    return value; // Do not store in this is dummy Map.
                }
            };
        } else {
            this.resourcesLastModifiedDates = new ConcurrentHashMap<>();
        }
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
        ZonedDateTime lastModifiedDate;
        try {
            if (request.isComponentStaticResourceRequest()) {
                // /public/components/...
                resourcePath = resolveResourceInComponent(app, request.getUriWithoutContextPath());
            } else if (request.isThemeStaticResourceRequest()) {
                // /public/themes/...
                resourcePath = resolveResourceInTheme(app, request.getUriWithoutContextPath());
            } else {
                // /public/...
                response.setContent(STATUS_BAD_REQUEST, "Invalid static resource URI '" + request.getUri() + "'.");
                return;
            }
            lastModifiedDate = resourcesLastModifiedDates.computeIfAbsent(resourcePath, this::getLastModifiedDate);
        } catch (IllegalArgumentException e) {
            // Invalid/incorrect static resource URI.
            response.setContent(STATUS_BAD_REQUEST, e.getMessage());
            return;
        } catch (ResourceNotFoundException e) {
            // Static resource file does not exists.
            response.setContent(STATUS_NOT_FOUND, "Requested resource '" + request.getUri() + "' does not exists.");
            return;
        } catch (Exception e) {
            // FileOperationException, IOException or any other Exception that might occur.
            log.error("An error occurred when manipulating paths for request '" + request + "'.", e);
            response.setContent(STATUS_INTERNAL_SERVER_ERROR,
                                "A server occurred while serving for static resource request '" + request + "'.");
            return;
        }

        if (lastModifiedDate == null) {
            // Since we failed to read last modified date of 'resourcePath' file, we cannot set cache headers.
            // Therefore just serve the file without any cache headers.
            response.setStatus(STATUS_OK);
            response.setContent(resourcePath, getContentType(request, resourcePath));
            return;
        }
        ZonedDateTime ifModifiedSinceDate = getIfModifiedSinceDate(request);
        if ((ifModifiedSinceDate != null) && Duration.between(ifModifiedSinceDate, lastModifiedDate).isZero()) {
            // Resource is NOT modified since the last serve.
            response.setStatus(STATUS_NOT_MODIFIED);
            return;
        }

        setCacheHeaders(lastModifiedDate, response);
        response.setStatus(STATUS_OK);
        response.setContent(resourcePath, getContentType(request, resourcePath));
    }

    private Path resolveResourceInComponent(App app, String uriWithoutContextPath) {
        /* Correct 'uriWithoutContextPath' value must be in
         * "/public/components/{component-context-path}/{fragment-simple-name}/{sub-directory}/{rest-of-the-path}"
         * format or in
         * "/public/components/{component-context-path}/base/{sub-directory}/{rest-of-the-path}" format.
         * So there should be at least 6 slashes. Don't worry about multiple consecutive slashes. They  are covered in
         * HttpRequest.isValid() method which is called before this method.
         */

        int slashesCount = 0, thirdSlashIndex = -1, fourthSlashIndex = -1, fifthSlashIndex = -1;
        for (int i = 0; i < uriWithoutContextPath.length(); i++) {
            if (uriWithoutContextPath.charAt(i) == '/') {
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
            throw new IllegalArgumentException("Invalid static resource URI '" + uriWithoutContextPath + "'.");
        }
        String componentContextPath = uriWithoutContextPath.substring(thirdSlashIndex, fourthSlashIndex);
        Component component = app.getComponents().get(componentContextPath);
        if (component == null) {
            throw new ResourceNotFoundException(
                    "Component for context path '" + componentContextPath + "' found in URI '" + uriWithoutContextPath +
                            "' does not exists in app '" + app.getName() + "'.");
        }

        // {fragment-simple-name} OR 'base'
        String fragmentSimpleName = uriWithoutContextPath.substring(fourthSlashIndex + 1, fifthSlashIndex);
        // {sub-directory}/{rest-of-the-path}
        String relativePathString = uriWithoutContextPath.substring(fifthSlashIndex + 1,
                                                                    uriWithoutContextPath.length());
        if (DIR_NAME_COMPONENT_RESOURCES.equals(fragmentSimpleName)) {
            // Resource is inside the 'base' directory of the 'component'
            return Paths.get(component.getPath(), DIR_NAME_PUBLIC_RESOURCES, relativePathString);
        } else {
            // Resource is inside the 'public' directory of fragment 'fragmentSimpleName' of the 'component'
            return Paths.get(component.getPath(), DIR_NAME_FRAGMENTS, fragmentSimpleName, DIR_NAME_PUBLIC_RESOURCES,
                             relativePathString);
        }
    }

    private Path resolveResourceInTheme(App app, String uriWithoutContextPath) {
        /* Correct 'uriWithoutContextPath' value must be in
         * "/public/themes/{theme-name}/{sub-directory}/{rest-of-the-path}" format.
         * So there should be at least 5 slashes. Don't worry about multiple consecutive slashes. They  are covered
         * in HttpRequest.isValid() method which is called before this method.
         */

        int slashesCount = 0, thirdSlashIndex = -1, fourthSlashIndex = -1;
        for (int i = 0; i < uriWithoutContextPath.length(); i++) {
            if (uriWithoutContextPath.charAt(i) == '/') {
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
            throw new IllegalArgumentException("Invalid static resource URI '" + uriWithoutContextPath + "'.");
        }
        String themeName = uriWithoutContextPath.substring(thirdSlashIndex + 1, fourthSlashIndex);
        Theme theme = app.getThemes().get(themeName);
        if (theme == null) {
            throw new ResourceNotFoundException("Theme '" + themeName + "' found in URI '" + uriWithoutContextPath +
                                                        "' does not exists in app '" + app.getName() + "'.");
        }

        // {sub-directory}/{rest-of-the-path}
        String relativePathString = uriWithoutContextPath.substring(fourthSlashIndex + 1,
                                                                    uriWithoutContextPath.length());
        return Paths.get(theme.getPath(), DIR_NAME_PUBLIC_RESOURCES, relativePathString);
    }

    private ZonedDateTime getLastModifiedDate(Path resourcePath) {
        if (!Files.isReadable(resourcePath)) {
            throw new ResourceNotFoundException("Static resource file '" + resourcePath + "' is not readable.");
        }

        BasicFileAttributes fileAttributes;
        try {
            fileAttributes = Files.readAttributes(resourcePath, BasicFileAttributes.class);
        } catch (NoSuchFileException | FileNotFoundException e) {
            // This shouldn't be happening because we checked the file's readability before. But just in case.
            throw new ResourceNotFoundException("Static resource file '" + resourcePath + "' does not exists.", e);
        } catch (Exception e) {
            // UnsupportedOperationException, IOException or any other Exception that might occur.
            throw new FileOperationException(
                    "Cannot read file attributes from static resource file '" + resourcePath + "'.", e);
        }
        if (fileAttributes.isRegularFile()) {
            return ZonedDateTime.ofInstant(fileAttributes.lastModifiedTime().toInstant(), GMT_TIME_ZONE);
        } else {
            /*
             * From book "OCP: Oracle Certified Professional Java SE 8 Programmer II Study Guide" page 478:
             *      Java defines a regular file as one that contains content, as opposed to a symbolic link,
             *      directory, resource (e.g. port, pipe), or other non-regular files that may be present in some
             *      operating systems. [...] It is possible for isRegularFile() to return true for a symbolic link,
             *      as long as the link resolves to a regular file.
             * Hence, checking 'isRegularFile' of a file is enough to determine its existence and not being a directory.
             */
            throw new ResourceNotFoundException("Static resource file '" + resourcePath + "' is not a regular file.");
        }
    }

    private ZonedDateTime getIfModifiedSinceDate(HttpRequest request) {
        // If-Modified-Since: Sat, 29 Oct 1994 19:43:31 GMT
        String ifModifiedSinceHeader = request.getHeaders().get("If-Modified-Since");
        if (ifModifiedSinceHeader == null) {
            return null; // 'If-Modified-Since' does not exists in HTTP headres.
        }
        try {
            return ZonedDateTime.parse(ifModifiedSinceHeader, HTTP_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error("Cannot parse 'If-Modified-Since' HTTP header value '" + ifModifiedSinceHeader + "'.", e);
            return null;
        }
    }

    private void setCacheHeaders(ZonedDateTime lastModifiedDate, HttpResponse response) {
        response.setHeader("Last-Modified", HTTP_DATE_FORMATTER.format(lastModifiedDate));
        response.setHeader("Cache-Control", "public,max-age=2592000");
    }

    private String getContentType(HttpRequest request, Path resource) {
        String extensionFromUri = FilenameUtils.getExtension(request.getUriWithoutContextPath());
        Optional<String> contentType = MimeMapper.getMimeType(extensionFromUri);
        if (contentType.isPresent()) {
            return contentType.get();
        }
        // Here 'resource' never null, thus 'FilenameUtils.getExtension(...)' never return null.
        String extensionFromPath = FilenameUtils.getExtension(resource.getFileName().toString());
        return MimeMapper.getMimeType(extensionFromPath).orElse(CONTENT_TYPE_WILDCARD);
    }
}