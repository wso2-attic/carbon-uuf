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

package org.wso2.carbon.uuf.connector.ms;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.wso2.carbon.uuf.internal.util.RequestUtil;

import javax.ws.rs.core.HttpHeaders;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HttpRequest implementation based on Microservice HTTP request.
 */
public class MicroserviceHttpRequest implements org.wso2.carbon.uuf.api.HttpRequest {

    private final String url;
    private final String method;
    private final String protocol;
    private final Map<String, String> headers;
    private final String uri;
    private final String appContext;
    private final String uriWithoutAppContext;
    private final String queryString;
    private final Map<String, Object> queryParams;
    private final byte[] contentBytes;
    private final int contentLength;
    private final InputStream inputStream;

    public MicroserviceHttpRequest(io.netty.handler.codec.http.HttpRequest request) {
        this(request, null);
    }

    public MicroserviceHttpRequest(io.netty.handler.codec.http.HttpRequest request, byte[] contentBytes) {
        this.url = null; // Netty HttpRequest does not have a 'getUrl()' method.
        this.method = request.getMethod().name();
        this.protocol = request.getProtocolVersion().text();
        this.headers = request.headers().entries().stream().collect(Collectors.toMap(Map.Entry::getKey,
                                                                                     Map.Entry::getValue));

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
        this.appContext = RequestUtil.getAppContext(this.uri);
        this.uriWithoutAppContext = RequestUtil.getUriWithoutAppContext(this.uri);
        this.queryString = rawQueryString; // Query string is not very useful, so we don't bother to decode it.
        if (rawQueryString != null) {
            HashMap<String, Object> map = new HashMap<>();
            new QueryStringDecoder(rawQueryString, false).parameters().forEach(
                    (key, value) -> map.put(key, (value.size() == 1) ? value.get(0) : value));
            this.queryParams = map;
        } else {
            this.queryParams = Collections.emptyMap();
        }
        if (contentBytes != null) {
            this.contentBytes = contentBytes;
            this.contentLength = contentBytes.length;
            this.inputStream = new ByteArrayInputStream(contentBytes);
        } else {
            this.contentBytes = null;
            this.contentLength = 0;
            this.inputStream = null;
        }
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getHostName() {
        String hostHeader = headers.get(HttpHeaders.HOST);
        return "//" + ((hostHeader == null) ? "localhost" : hostHeader);
    }

    @Override
    public String getCookieValue(String cookieName) {
        String cookieHeader = headers.get(HttpHeaders.COOKIE);
        if (cookieHeader == null) {
            return null;
        }
        return ServerCookieDecoder.STRICT.decode(cookieHeader).stream()
                .filter(cookie -> cookie.name().equals(cookieName))
                .findFirst()
                .map(Cookie::value).orElse(null);
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getAppContext() {
        return appContext;
    }

    @Override
    public String getUriWithoutAppContext() {
        return uriWithoutAppContext;
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
    public String getContentType() {
        return headers.get(HttpHeaders.CONTENT_TYPE);
    }

    @Override
    public String getContent() {
        return new String(contentBytes);
    }

    @Override
    public byte[] getContentBytes() {
        return contentBytes;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException("Netty HttpRequest does not have enough information.");
    }

    @Override
    public String getRemoteAddr() {
        throw new UnsupportedOperationException("Netty HttpRequest does not have enough information.");
    }

    @Override
    public String getContextPath() {
        throw new UnsupportedOperationException("Netty HttpRequest does not have enough information.");
    }

    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException("Netty HttpRequest does not have enough information.");
    }

    @Override
    public String toString() {
        return "{\"method\": \"" + method + "\", \"protocol\": \"" + protocol + "\", \"uri\": \"" + uri +
                "\", \"queryString\": \"" + queryString + "\"}";
    }
}
