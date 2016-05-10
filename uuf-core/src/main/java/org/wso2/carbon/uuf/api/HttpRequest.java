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

import io.netty.handler.codec.http.QueryStringDecoder;
import org.wso2.carbon.uuf.core.exception.PageNotFoundException;
import org.wso2.carbon.uuf.core.exception.PageRedirectException;

import javax.ws.rs.core.HttpHeaders;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequest {

    private final String method;
    private final String protocol;
    private final String queryString;
    private final byte[] content;
    private final String contentType;
    private final long contentLength;
    private final String requestURI;
    private final String requestURL;
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

    /**
     * Returns the method of the request
     *
     * @return http method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Returns the protocol of the request
     *
     * @return protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Returns the query String of the request
     *
     * @return query string
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * Returns the content of the request as string.
     *
     * @return converted content string
     */
    public String getContent() {
        return new String(content);
    }

    /**
     * Returns the content of the request as array of bytes.
     *
     * @return array of bytes
     */
    public byte[] getContentBytes() {
        return content;
    }

    /**
     * Returns the content Type of the request.
     *
     * @return content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the content length of the request.
     *
     * @return content length
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Returns the part of this request's URL from the protocol name up to the query String in the first line of the
     * HTTP request.
     *
     * @return request uri
     */
    public String getRequestURI() {
        String uri = requestURI.replaceAll("/+", "/");
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        };
        return uri;
    }

    /**
     * Reconstructs the URL the client used to make the request.
     *
     * @return request url
     */
    public String getRequestURL() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true when https is used.
     *
     * @return true when https, false otherwise
     */
    public boolean isSecure() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the Internet Protocol (IP) address of the client that sent the request.
     *
     * @return client ip address
     */
    public String getRemoteAddr() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the portion of the request URI that indicates the context of the request.
     * eg. /uuf-app
     *
     * @return context path
     */
    public String getContextPath() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the request local port number.
     *
     * @return port number
     */
    public int getLocalPort() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieves the body of the request as binary data.
     *
     * @return
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Retrieves the map of headers.
     *
     * @return
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Returns hostname. If host header is not found, returns //localhost
     *
     * @return
     */
    public String getHostName() {
        String hostHeader = headers.get(HttpHeaders.HOST);
        String host = "//" + ((hostHeader == null) ? "localhost" : hostHeader);
        return host;
    }

    /**
     * Constructs a HTTP request using netty http request.
     *
     * @param request
     */
    public HttpRequest(io.netty.handler.codec.http.HttpRequest request) {
        this.method = request.getMethod().name();
        this.protocol = request.getProtocolVersion().text();
        this.queryString = QueryStringDecoder.decodeComponent(request.getUri());
//        ByteBuf buf = request.content();
//        this.content = buf.array();
//        this.contentType = request.headers().get(HttpHeaders.CONTENT_TYPE);
//        this.contentLength = content.length;
        this.content = null;
        this.contentLength = 0;
        this.inputStream = null;
        //
        this.contentType = request.headers().get(HttpHeaders.CONTENT_TYPE);
        this.requestURI = request.getUri();
        this.headers = request.headers().entries().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        //not implemented yet
        this.requestURL = null;
        this.isSecure = false;
        this.remoteAddr = null;
        this.contextPath = null;
        this.localPort = -1;
    }

    public URIComponents getUriComponents() {
        String uri = getRequestURI();
        int firstSlash = uri.indexOf('/', 1);

        if (firstSlash < 0) {
            if (uri.equals("/favicon.ico")) {
                //TODO: send a favicon, cacheable favicon avoids frequent requests for it.
                throw new PageNotFoundException("");
            }

            // eg: url = http://example.com/app and uri = /app
            // since we don't support ROOT app, this must be a mis-type
            throw new PageRedirectException(uri + "/");
        }

        String appName = uri.substring(1, firstSlash);
        String appContext = uri.substring(0, firstSlash);
        String uriWithoutAppContext = uri.substring(firstSlash, uri.length());
        return new URIComponents(appName, appContext, uriWithoutAppContext);
    }

    public static class URIComponents {
        private String appName;
        private String appContext;
        private String uriWithoutAppContext;

        public URIComponents(String appName, String appContext, String uriWithoutAppContext) {
            this.appName = appName;
            this.appContext = appContext;
            this.uriWithoutAppContext = uriWithoutAppContext;
        }

        public String getAppName() {
            return appName;
        }

        public String getAppContext() {
            return appContext;
        }

        public String getUriWithoutAppContext() {
            return uriWithoutAppContext;
        }
    }

}
