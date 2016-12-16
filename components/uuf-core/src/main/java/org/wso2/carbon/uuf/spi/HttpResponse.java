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
import java.util.Map;

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

    void setStatus(int statusCode);

    int getStatus();

    default void setContent(String content) {
        setContent(content, CONTENT_TYPE_TEXT_PLAIN);
    }

    void setContent(String content, String contentType);

    void setContent(File content);

    void setContent(File content, String contentType);

    void setContent(Object content, String contentType);

    default void setContent(Path content) {
        setContent(content.toFile());
    }

    default void setContent(Path content, String contentType) {
        setContent(content.toFile(), contentType);
    }

    void setContent(InputStream content, String contentType);

    default void setContent(int statusCode, String content) {
        setStatus(statusCode);
        setContent(content);
    }

    default void setContent(int statusCode, String content, String contentType) {
        setStatus(statusCode);
        setContent(content);
        setContentType(contentType);
    }

    Object getContent();

    void setContentType(String type);

    String getContentType();

    void setHeader(String name, String value);

    Map<String, String> getHeaders();
}
