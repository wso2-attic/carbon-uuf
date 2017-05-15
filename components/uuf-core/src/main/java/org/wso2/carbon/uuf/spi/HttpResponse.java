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

package org.wso2.carbon.uuf.spi;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Represents a HTTP response.
 *
 * @since 1.0.0
 */
public interface HttpResponse {

    int STATUS_OK = 200;
    int STATUS_MOVED_PERMANENTLY = 301;
    int STATUS_FOUND = 302;
    int STATUS_NOT_MODIFIED = 304;
    int STATUS_BAD_REQUEST = 400;
    int STATUS_UNAUTHORIZED = 401;
    int STATUS_FORBIDDEN = 403;
    int STATUS_NOT_FOUND = 404;
    int STATUS_INTERNAL_SERVER_ERROR = 500;

    String CONTENT_TYPE_WILDCARD = "*/*";
    String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    String CONTENT_TYPE_TEXT_HTML = "text/html";
    String CONTENT_TYPE_IMAGE_PNG = "image/png";
    String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    String HEADER_LOCATION = "Location";
    String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    String HEADER_X_XSS_PROTECTION = "X-XSS-Protection";
    String HEADER_CACHE_CONTROL = "Cache-Control";
    String HEADER_LAST_MODIFIED = "Last-Modified";
    String HEADER_EXPIRES = "Expires";
    String HEADER_PRAGMA = "Pragma";
    String HEADER_UPGRADE_INSECURE_REQUESTS = "Upgrade-Insecure-Requests";

    /**
     * Sets the <a href="https://tools.ietf.org/html/rfc2616#section-10">HTTP status code</a> of this response to the
     * specified integer.
     *
     * @param statusCode HTTP status code to be set
     */
    void setStatus(int statusCode);

    /**
     * Returns the <a href="https://tools.ietf.org/html/rfc2616#section-10">HTTP status code</a> of this response.
     *
     * @return HTTP status code of this response
     */
    int getStatus();

    /**
     * Sets the specified textual content to this response. This is equivalent to
     * {@code setContent(content, }{@link #CONTENT_TYPE_TEXT_PLAIN}{@code )}
     *
     * @param content textual content to be set
     * @see #setContent(String, String)
     */
    default void setContent(String content) {
        setContent(content, CONTENT_TYPE_TEXT_PLAIN);
    }

    /**
     * Sets the specified textual content and the content type to this response.
     *
     * @param content     textual content to be set
     * @param contentType MIME type of the content
     */
    void setContent(String content, String contentType);

    /**
     * Sets the specified file content to this response.
     *
     * @param content file content to be set
     */
    void setContent(File content);

    /**
     * Sets the specified file content and the content type to this response.
     *
     * @param content     file content to be set
     * @param contentType MIME type of the content
     */
    void setContent(File content, String contentType);

    /**
     * Sets the specified content and the content type to this response.
     *
     * @param content     content to be set
     * @param contentType MIME type of the content
     */
    void setContent(Object content, String contentType);

    /**
     * Sets the file content located by the specified path to this response.
     *
     * @param content path of the file content to be set
     */
    default void setContent(Path content) {
        setContent(content.toFile());
    }

    /**
     * Sets the file content located by the specified path and the content type to this response.
     *
     * @param content     path of the file content to be set
     * @param contentType MIME type of the content
     */
    default void setContent(Path content, String contentType) {
        setContent(content.toFile(), contentType);
    }

    /**
     * Sets the content read through the specified input stream and the content type to this response.
     *
     * @param content     input stream to the content to be set
     * @param contentType MIME type of the content
     */
    void setContent(InputStream content, String contentType);

    /**
     * Sets the specified the HTTP status code and the textual content to this response.
     *
     * @param statusCode HTTP status code to be set
     * @param content    textual content to be set
     */
    default void setContent(int statusCode, String content) {
        setStatus(statusCode);
        setContent(content);
    }

    /**
     * Sets the specified the HTTP status code and the textual content to this response.
     *
     * @param statusCode  HTTP status code to be set
     * @param content     textual content to be set
     * @param contentType MIME type of the content
     */
    default void setContent(int statusCode, String content, String contentType) {
        setStatus(statusCode);
        setContent(content);
        setContentType(contentType);
    }

    /**
     * Returns the content of this response.
     *
     * @return the content of this response
     */
    Object getContent();

    /**
     * Sets the <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">MIME type</a> of this response.
     *
     * @param contentType MIME type to be set
     */
    void setContentType(String contentType);

    /**
     * Returns the <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">MIME type</a> of this
     * response.
     *
     * @return MIME type of this response
     */
    String getContentType();

    /**
     * Adds a new value to the specified HTTP header of this response.
     *
     * @param name  name of the HTTP header
     * @param value value to be added; if {@code null} existing value(s) will be removed
     */
    void setHeader(String name, String value);

    /**
     * Returns the HTTP headers of this response.
     *
     * @return HTTP headers of this response.
     */
    MultivaluedMap<String, String> getHeaders();

    /**
     * Adds a cookie to this response.
     *
     * @param name  name of the cookie
     * @param value value of the cookie
     */
    void addCookie(String name, String value);

    /**
     * Returns the value of the specified cookie of this response.
     *
     * @param name name of the cookie
     * @return value of the specified cookie of this response
     */
    String getCookie(String name);
}
