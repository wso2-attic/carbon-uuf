package org.wso2.carbon.uuf.core;

import io.netty.handler.codec.http.HttpRequest;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequestLookup {
    private final String appContext;
    private final HttpRequest request;
    private final Deque<String> publicUriStack;
    private final Map<String, StringBuilder> placeholderBuffers;
    private final Map<String, String> zoneContents;

    public RequestLookup(String appContext, HttpRequest request) {
        this.appContext = appContext;
        this.request = request;
        this.publicUriStack = new ArrayDeque<>();
        this.placeholderBuffers = new HashMap<>();
        this.zoneContents = new HashMap<>();
    }

    public HttpRequest getRequest() {
        return request;
    }

    public String getAppContext() {
        return appContext;
    }

    void pushToPublicUriStack(String publicUri) {
        publicUriStack.push(publicUri);
    }

    String popPublicUriStack() {
        return publicUriStack.pop();
    }

    public String getPublicUri() {
        return publicUriStack.peekLast();
    }

    public void addToPlaceholder(String placeholderName, String content) {
        StringBuilder buffer = placeholderBuffers.get(placeholderName);
        if (buffer == null) {
            buffer = new StringBuilder(content);
            placeholderBuffers.put(placeholderName, buffer);
        } else {
            buffer.append(content);
        }
    }

    public Optional<String> getPlaceholderContent(String placeholderName) {
        StringBuilder buffer = placeholderBuffers.get(placeholderName);
        return (buffer == null) ? Optional.<String>empty() : Optional.of(buffer.toString());
    }

    public Map<String, String> getPlaceholderContents() {
        Map<String, String> placeholderContents = new HashMap<>(placeholderBuffers.size());
        for (Map.Entry<String, StringBuilder> entry : placeholderBuffers.entrySet()) {
            placeholderContents.put(entry.getKey(), entry.getValue().toString());
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
}
