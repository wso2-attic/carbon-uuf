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
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FormItem;
import org.wso2.msf4j.formparam.FormParamIterator;
import org.wso2.msf4j.formparam.exception.FormUploadException;
import org.wso2.msf4j.formparam.util.StreamUtil;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * UUF HttpRequest implementation based on MSF4J request.
 */
public class MicroserviceHttpRequest implements HttpRequest {

    public static final String PROPERTY_KEY_HTTP_VERSION = "HTTP_VERSION";
    public static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
    private static final Logger log = LoggerFactory.getLogger(MicroserviceHttpRequest.class);

    private final String url;
    private final String method;
    private final String protocol;
    private final Map<String, String> headers;
    private final String uri;
    private final String contextPath;
    private final String uriWithoutContextPath;
    private final String queryString;
    private final Map<String, Object> queryParams;
    private final int contentLength;
    private final Map<String, Object> formParams;
    private final Map<String, Object> files;

    public MicroserviceHttpRequest(Request request) {
        this(request, null);
    }

    public MicroserviceHttpRequest(Request request, FormParamIterator formParamIterator) {
        this.url = null; // MSF4J Request does not have a 'getUrl()' method.
        this.method = request.getHttpMethod();
        this.protocol = request.getProperty(PROPERTY_KEY_HTTP_VERSION).toString();
        this.headers = request.getHeaders();

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
            new QueryStringDecoder(rawQueryString, false).parameters().forEach(
                    (key, value) -> map.put(key, (value.size() == 1) ? value.get(0) : value));
            this.queryParams = map;
        } else {
            this.queryParams = Collections.emptyMap();
        }

        // POST form params
        if (formParamIterator != null) {
            this.formParams = new HashMap<>();
            this.files = new HashMap<>();
            while (formParamIterator.hasNext()) {
                FormItem item = formParamIterator.next();
                InputStream inputStream = null;
                try {
                    inputStream = item.openStream();
                    if (item.isFormField()) {
                        this.formParams.put(item.getFieldName(), StreamUtil.asString(inputStream));
                    } else {
                        this.files.put(item.getName(), inputStream);
                    }
                } catch (FormUploadException | IOException e) {
                    // respond back to client without further processing
                    throw new WebApplicationException(
                            "An error occurred while processing POST param '" + item.getFieldName() + "'.", e);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }
        } else {
            this.formParams = Collections.emptyMap();
            this.files = Collections.emptyMap();
        }

        // process content length
        String contentLengthHeaderVal = this.headers.get(HTTP_HEADER_CONTENT_LENGTH);
        try {
            this.contentLength = (contentLengthHeaderVal == null) ? 0 : Integer.parseInt(contentLengthHeaderVal);
        } catch (NumberFormatException e) {
            throw new WebApplicationException(
                    "Cannot parse 'Content-Length' header value '" + contentLengthHeaderVal + "' as an integer.", e);
        }
    }

    private String constructUrl(boolean isSecured, String localAddr, String localPort, String uri) {
        return "http" + ((isSecured) ? "s" : "") + "://" + localAddr + ":" + localPort + uri;
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
    public String getContentType() {
        return headers.get(HttpHeaders.CONTENT_TYPE);
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
    public int getLocalPort() {
        throw new UnsupportedOperationException("Netty HttpRequest does not have enough information.");
    }

    @Override
    public String toString() {
        return "{\"method\": \"" + method + "\", \"protocol\": \"" + protocol + "\", \"uri\": \"" + uri +
                "\", \"queryString\": \"" + queryString + "\"}";
    }
}
