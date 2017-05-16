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

import org.apache.commons.io.FilenameUtils;
import org.wso2.carbon.uuf.spi.HttpResponse;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * UUF HttpResponse implementation based on JAX-RS Response.
 */
public class MicroserviceHttpResponse implements HttpResponse {

    private int status;
    private Object content;
    private String contentType;
    private MultivaluedMap<String, String> headers;
    private Map<String, String> cookies;

    public MicroserviceHttpResponse() {
        this.status = 200;
        this.headers = new MultivaluedHashMap<>();
        this.cookies = new HashMap<>();
    }

    @Override
    public void setStatus(int statusCode) {
        this.status = statusCode;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setContent(String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public void setContent(File content) {
        String extension = FilenameUtils.getExtension(content.getName());
        setContent(content, extension.isEmpty() ? CONTENT_TYPE_WILDCARD : extension);
    }

    @Override
    public void setContent(File content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public void setContent(InputStream content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public void setContent(Object content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setHeader(String name, String value) {
        headers.add(name, value);
    }

    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    public void addCookie(String name, String value) {
        cookies.put(name, value);
    }

    @Override
    public void addCookie(String name, String value, String path, boolean isSecure, boolean isHttpOnly) {
        if (path != null) {
            value += "; Path=" + path;
        }
        if (isSecure) {
            value += "; Secure";
        }
        if (isHttpOnly) {
            value += "; HTTPOnly";
        }
        addCookie(name, value);
    }

    @Override
    public String getCookie(String name) {
        return cookies.get(name);
    }

    public Response build() {
        Response.ResponseBuilder responseBuilder = Response.status(status);
        if (content != null) {
            responseBuilder.entity(content).type(contentType);
        }
        headers.entrySet().forEach(entry -> responseBuilder.header(entry.getKey(), entry.getValue()));
        cookies.entrySet().forEach(entry -> responseBuilder.cookie(new NewCookie(entry.getKey(), entry.getValue())));
        return responseBuilder.build();
    }
}
