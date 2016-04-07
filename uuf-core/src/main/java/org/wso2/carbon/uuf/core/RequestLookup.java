package org.wso2.carbon.uuf.core;

import io.netty.handler.codec.http.HttpRequest;

import java.util.ArrayDeque;
import java.util.Deque;

public class RequestLookup {
    private final String appContext;
    private final HttpRequest request;
    private final Deque<String> publicUriStack;

    public RequestLookup(String appContext, HttpRequest request) {
        this.appContext = appContext;
        this.request = request;
        this.publicUriStack = new ArrayDeque<>();
    }

    public HttpRequest getRequest() {
        return request;
    }

    public String getAppContext() {
        return appContext;
    }

    public void pushToPublicUriStack(String publicUri) {
        publicUriStack.push(publicUri);
    }

    public String popPublicUriStack() {
        return publicUriStack.pop();
    }

    public String getPublicUri() {
        return publicUriStack.peekLast();
    }
}
