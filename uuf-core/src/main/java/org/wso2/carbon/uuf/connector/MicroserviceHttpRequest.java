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

package org.wso2.carbon.uuf.connector;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.wso2.carbon.uuf.api.HttpRequest;
import org.wso2.carbon.uuf.internal.util.RequestUtil;

import javax.ws.rs.core.HttpHeaders;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * HttpRequest implementation based on Microservice HTTP request.
 */
public class MicroserviceHttpRequest implements HttpRequest {
    private final String method;
    private final String protocol;
    private final String queryString;
    private final byte[] content;
    private final String contentType;
    private final long contentLength;
    private final String url;
    private final String uri;
    private final String appContext;
    private final String uriWithoutAppContext;
    private final boolean isSecure;
    private final String remoteAddr;
    private final String contextPath;
    private final int localPort;
    private final InputStream inputStream;
    private final Map<String, String> headers;
    //TODO: Following need to be implemented.
    //getPathTranslated
    //getHeader
    //getAllHeaders
    //getParameter
    //getAllParameters
    //getLocale
    //getAllLocales
    //getMappedPath
    //getFile(formFeildName)
    //getAllFiles()
    //getCookie(name)
    //getAllCookies

    public MicroserviceHttpRequest(io.netty.handler.codec.http.HttpRequest request, byte[] content) {
        this.method = request.getMethod().name();
        this.protocol = request.getProtocolVersion().text();
        this.queryString = QueryStringDecoder.decodeComponent(request.getUri());
        if (content != null) {
            this.content = content;
            this.contentLength = content.length;
            this.inputStream = new ByteArrayInputStream(content);
        } else {
            this.content = null;
            this.contentLength = 0;
            this.inputStream = null;
        }
        //
        this.contentType = request.headers().get(HttpHeaders.CONTENT_TYPE);
        this.uri = request.getUri();
        this.appContext = RequestUtil.getAppContext(this.uri);
        this.uriWithoutAppContext = RequestUtil.getUriWithoutAppContext(this.uri);
        this.headers = request.headers().entries().stream().collect(Collectors.toMap(Map.Entry::getKey,
                                                                                     Map.Entry::getValue));
        //not implemented yet
        this.url = null;
        this.isSecure = false;
        this.remoteAddr = null;
        this.contextPath = null;
        this.localPort = -1;
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
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getContent() {
        return new String(content);
    }

    @Override
    public byte[] getContentBytes() {
        return content;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public long getContentLength() {
        return contentLength;
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
    public String getUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteAddr() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContextPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
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
    public Optional<String> getCookieValue(String cookieName) {
        String cookieHeader = headers.get(HttpHeaders.COOKIE);
        if (cookieHeader == null) {
            return Optional.<String>empty();
        }
        return ServerCookieDecoder.STRICT.decode(cookieHeader).stream()
                .filter(cookie -> cookie.name().equals(cookieName))
                .findFirst()
                .map(Cookie::value);
    }

    @Override
    public String toString() {
        return "{\"method\": \"" + method + "\", \"url\": \"" + url + "\", \"protocol\": \"" + protocol +
                "\", \"host\": \"" + getHostName() + "\"}";
    }
}
