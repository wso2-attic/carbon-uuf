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

package org.wso2.carbon.uuf.api;

import org.wso2.carbon.uuf.internal.util.UriUtils;

import java.io.InputStream;
import java.util.Map;

/**
 * Provides a common interface to provide request information.
 */
public interface HttpRequest {

    /**
     * Reconstructs the URL the client used to make the request.
     *
     * @return request url
     */
    String getUrl();

    /**
     * Returns the method of the request
     *
     * @return http method
     */
    String getMethod();

    /**
     * Returns the protocol of the request
     *
     * @return protocol
     */
    String getProtocol();

    /**
     * Retrieves the map of headers.
     *
     * @return
     */
    Map<String, String> getHeaders();

    /**
     * Returns hostname. If host header is not found, returns //localhost
     *
     * @return
     */
    String getHostName();

    String getCookieValue(String cookieName);

    /**
     * Returns the part of this request's URL from the protocol name up to the query String in the first line of the
     * HTTP request.
     *
     * @return request uri
     */
    String getUri();

    String getAppContext();

    String getUriWithoutAppContext();

    /**
     * Returns the query String of the request
     *
     * @return query string
     */
    String getQueryString();

    Map<String, Object> getQueryParams();

    /**
     * Returns the content Type of the request.
     *
     * @return content type
     */
    String getContentType();

    /**
     * Returns the content of the request as string.
     *
     * @return converted content string
     */
    String getContent();

    /**
     * Returns the content of the request as array of bytes.
     *
     * @return array of bytes
     */
    byte[] getContentBytes();

    /**
     * Retrieves the body of the request as binary data.
     *
     * @return
     */
    InputStream getInputStream();

    /**
     * Returns the content length of the request.
     *
     * @return content length
     */
    long getContentLength();

    /**
     * Returns true when https is used.
     *
     * @return true when https, false otherwise
     */
    boolean isSecure();

    /**
     * Returns the Internet Protocol (IP) address of the client that sent the request.
     *
     * @return client ip address
     */
    String getRemoteAddr();

    /**
     * Returns the portion of the request URI that indicates the context of the request. eg. /uuf-app
     *
     * @return context path
     */
    String getContextPath();

    /**
     * Returns the request local port number.
     *
     * @return port number
     */
    int getLocalPort();

    default boolean isValid() {
        String uri = getUri();

        // An URI must begin with '/ & it should have at least two characters.
        if ((uri.length() < 2) || (uri.charAt(0) != '/')) {
            return false;
        }

        // '//' or '..' are not allowed in URIs.
        boolean isPreviousCharInvalid = false;
        for (int i = 0; i < uri.length(); i++) {
            char currentChar = uri.charAt(i);
            if ((currentChar == '/') || (currentChar == '.')) {
                if (isPreviousCharInvalid) {
                    return false;
                } else {
                    isPreviousCharInvalid = true;
                }
            } else {
                isPreviousCharInvalid = false;
            }
        }
        return true;
    }

    default boolean isStaticResourceRequest() {
        return getUriWithoutAppContext().startsWith("/public/");
    }

    default boolean isComponentStaticResourceRequest() {
        return getUriWithoutAppContext().startsWith(UriUtils.COMPONENT_STATIC_RESOURCES_URI_PREFIX);
    }

    default boolean isThemeStaticResourceRequest() {
        return getUriWithoutAppContext().startsWith(UriUtils.THEMES_STATIC_RESOURCES_URI_PREFIX);
    }

    default boolean isDebugRequest() {
        return getUriWithoutAppContext().startsWith(UriUtils.DEBUG_APP_URI_PREFIX);
    }

    default boolean isFragmentRequest() {
        return getUriWithoutAppContext().startsWith(UriUtils.FRAGMENTS_URI_PREFIX);
    }

    default boolean isDefaultFaviconRequest() {
        return getUri().equals("/favicon.ico");
    }
}
