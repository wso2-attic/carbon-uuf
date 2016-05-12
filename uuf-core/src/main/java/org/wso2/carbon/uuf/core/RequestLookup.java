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

package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.api.HttpRequest;
import org.wso2.carbon.uuf.api.Placeholder;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequestLookup {

    private final String appContext;
    private final HttpRequest request;
    private Map<String, String> uriParams;
    private final Deque<String> publicUriStack;
    private final EnumMap<Placeholder, StringBuilder> placeholderBuffers;
    private final Map<String, String> zoneContents;
    private Map<String, String> responseHeaders = new HashMap<>();

    public RequestLookup(HttpRequest request) {
        this.appContext = request.getAppContext();
        this.request = request;
        this.publicUriStack = new ArrayDeque<>();
        this.placeholderBuffers = new EnumMap<>(Placeholder.class);
        this.zoneContents = new HashMap<>();
    }

    public String getAppContext() {
        return appContext;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public Map<String, String> getUriParams() {
        return uriParams;
    }

    void setUriParams(Map<String, String> uriParams) {
        this.uriParams = uriParams;
    }

    void pushToPublicUriStack(String publicUri) {
        publicUriStack.addLast(publicUri);
    }

    String popPublicUriStack() {
        return publicUriStack.removeLast();
    }

    public String getPublicUri() {
        return publicUriStack.peekLast();
    }

    public void addToPlaceholder(Placeholder placeholder, String content) {
        StringBuilder buffer = placeholderBuffers.get(placeholder);
        if (buffer == null) {
            buffer = new StringBuilder(content);
            placeholderBuffers.put(placeholder, buffer);
        } else {
            buffer.append(content);
        }
    }

    public Optional<String> getPlaceholderContent(Placeholder placeholder) {
        StringBuilder buffer = placeholderBuffers.get(placeholder);
        return (buffer == null) ? Optional.<String>empty() : Optional.of(buffer.toString());
    }

    public Map<String, String> getPlaceholderContents() {
        Map<String, String> placeholderContents = new HashMap<>(placeholderBuffers.size());
        for (Map.Entry<Placeholder, StringBuilder> entry : placeholderBuffers.entrySet()) {
            placeholderContents.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return placeholderContents;
    }

    public void putToZone(String zoneName, String content) {
        String currentContent = zoneContents.get(zoneName);
        if (currentContent == null) {
            zoneContents.put(zoneName, content);
        } else {
            throw new IllegalStateException("Zone '" + zoneName + "' is already filled with content.");
        }
    }

    public Optional<String> getZoneContent(String zoneName) {
        return Optional.ofNullable(zoneContents.get(zoneName));
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Sets a HTTP response header Access level is package protected since only 'org.wso2.carbon.uuf.core' can access.
     *
     * @param name
     * @param value
     */
    void setResponseHeader(String name, String value){
        responseHeaders.put(name, value);
    }
}
