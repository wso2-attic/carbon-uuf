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

package org.wso2.carbon.uuf.httpconnector.msf4j;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UUF HttpRequest implementation based on MSF4J request.
 */
public class MicroserviceHttpRequest implements HttpRequest {

    private static final String PROPERTY_HTTP_VERSION = "HTTP_VERSION";
    private static final String PROPERTY_LOCAL_NAME = "LOCAL_NAME";
    private static final String PROPERTY_IS_SECURED_CONNECTION = "IS_SECURED_CONNECTION";
    private static final String PROPERTY_LISTENER_PORT = "LISTENER_PORT";
    private static final String PROPERTY_REMOTE_HOST = "REMOTE_HOST";
    private static final String PROPERTY_REMOTE_PORT = "REMOTE_PORT";

    private final Request msf4jRequest;
    private final String method;
    private final Map<String, Cookie> cookies;
    private final Map<String, String> headers;
    private final String uri;
    private final String contextPath;
    private final String uriWithoutContextPath;
    private final String queryString;
    private final Map<String, Object> queryParams;
    private final Map<String, Object> formParams;
    private final Map<String, Object> files;

    public MicroserviceHttpRequest(Request request) {
        this(request, null);
    }

    public MicroserviceHttpRequest(Request request, MultivaluedMap<String, ?> postParams) {
        this.msf4jRequest = request;
        this.method = request.getHttpMethod();

        // process URI
        String rawUri = request.getUri();
        int uriPathEndIndex = rawUri.indexOf('?');
        String rawUriPath, rawQueryString;
        if (uriPathEndIndex == -1) {
            rawUriPath = rawUri;
            rawQueryString = null;
        } else {
            rawUriPath = rawUri.substring(0, uriPathEndIndex);
            rawQueryString = rawUri.substring(uriPathEndIndex + 1, rawUri.length());
        }
        this.uri = QueryStringDecoder.decodeComponent(rawUriPath);
        this.contextPath = HttpRequest.getContextPath(this.uri);
        this.uriWithoutContextPath = HttpRequest.getUriWithoutContextPath(this.uri);
        this.queryString = rawQueryString; // Query string is not very useful, so we don't bother to decode it.
        if (rawQueryString != null) {
            HashMap<String, Object> map = new HashMap<>();
            new QueryStringDecoder(rawQueryString, false).parameters()
                    .forEach((key, value) -> map.put(key, (value.size() == 1) ? value.get(0) : value));
            this.queryParams = map;
        } else {
            this.queryParams = Collections.emptyMap();
        }

        // process headers and cookies
        this.headers = request.getHeaders();
        String cookieHeader = this.headers.get(HttpHeaders.COOKIE);
        this.cookies = (cookieHeader == null) ? Collections.emptyMap() :
                ServerCookieDecoder.STRICT.decode(cookieHeader).stream().collect(Collectors.toMap(Cookie::name,
                                                                                                  c -> c));

        // process form POST form data
        if (postParams == null) {
            this.formParams = Collections.emptyMap();
            this.files = Collections.emptyMap();
        } else {
            this.formParams = new HashMap<>();
            this.files = new HashMap<>();
            for (Map.Entry<String, ? extends List<?>> entry : postParams.entrySet()) {
                List<?> values = entry.getValue();
                if (values.isEmpty()) {
                    continue;
                }
                if (values.get(0) instanceof File) {
                    this.files.put(entry.getKey(), (values.size() == 1) ? values.get(0) : values);
                } else {
                    this.formParams.put(entry.getKey(), (values.size() == 1) ? values.get(0) : values);
                }
            }
        }
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getProtocol() {
        return (String) msf4jRequest.getProperty(PROPERTY_HTTP_VERSION);
    }

    @Override
    public boolean isSecure() {
        return (Boolean) msf4jRequest.getProperty(PROPERTY_IS_SECURED_CONNECTION);
    }

    @Override
    public String getUrl() {
        throw new UnsupportedOperationException("To be implemented");
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getUriWithoutContextPath() {
        return uriWithoutContextPath;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getCookieValue(String cookieName) {
        Cookie cookie = cookies.get(cookieName);
        return (cookie == null) ? null : cookie.value();
    }

    @Override
    public String getContentType() {
        return headers.get(HEADER_CONTENT_TYPE);
    }

    @Override
    public long getContentLength() {
        String contentLengthHeader = headers.get(HEADER_CONTENT_LENGTH);
        return (contentLengthHeader == null) ? -1 : Long.parseLong(contentLengthHeader);
    }

    @Override
    public Map<String, Object> getFormParams() {
        return formParams;
    }

    @Override
    public Map<String, Object> getFiles() {
        return files;
    }

    @Override
    public String getLocalAddress() {
        return (String) msf4jRequest.getProperty(PROPERTY_LOCAL_NAME);
    }

    @Override
    public int getLocalPort() {
        return (Integer) msf4jRequest.getProperty(PROPERTY_LISTENER_PORT);
    }

    @Override
    public String getRemoteAddress() {
        return (String) msf4jRequest.getProperty(PROPERTY_REMOTE_HOST);
    }

    @Override
    public int getRemotePort() {
        return (Integer) msf4jRequest.getProperty(PROPERTY_REMOTE_PORT);
    }

    @Override
    public String toString() {
        return "{\"method\": \"" + method + "\", \"uri\": \"" + uri + "\", \"query\": \"" + queryString +
                "\", \"protocol\": \"" + getProtocol() + "\"}";
    }
}
