package org.wso2.carbon.uuf.core;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.ArrayDeque;
import java.util.Deque;

public class RequestLookup {
    private final HttpRequest request;
    private final HttpResponse response;
    private final Deque<Page> pagesStack;
    private final Deque<Fragment> fragmentsStack;

    public RequestLookup(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
        this.pagesStack = new ArrayDeque<>();
        this.fragmentsStack = new ArrayDeque<>();
    }

    public HttpRequest getRequest() {
        return request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public Deque<Page> getPagesStack() {
        return pagesStack;
    }

    public Deque<Fragment> getFragmentsStack() {
        return fragmentsStack;
    }
}
