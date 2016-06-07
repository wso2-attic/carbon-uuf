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
import org.wso2.carbon.uuf.api.HttpResponse;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.internal.util.NameUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequestLookup {

    private final String appContext;
    private final HttpRequest request;
    private final HttpResponse response;
    private Map<String, String> pathParams;
    private final RenderingFlowTracker renderingFlowTracker;
    private final Deque<String> publicUriStack;
    private final EnumMap<Placeholder, StringBuilder> placeholderBuffers;
    private final Map<String, String> zoneContents;

    public RequestLookup(String appContext, HttpRequest request, HttpResponse response) {
        this.appContext = (appContext == null) ? request.getAppContext() : appContext;
        this.request = request;
        this.response = response;
        this.renderingFlowTracker = new RenderingFlowTracker();
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

    public HttpResponse getResponse() {
        return response;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    void setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams;
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

    void pushToPublicUriStack(String publicUri) {
        publicUriStack.addLast(appContext + publicUri);
    }

    public String getPublicUri() {
        return publicUriStack.peekLast();
    }

    String popPublicUriStack() {
        return publicUriStack.removeLast();
    }

    public RenderingFlowTracker tracker() {
        return renderingFlowTracker;
    }

    public static class RenderingFlowTracker {

        private static final Integer TYPE_PAGE = 2;
        private static final Integer TYPE_FRAGMENT = 3;
        private static final Integer TYPE_LAYOUT = 4;

        private final Deque<String> componentNamesStack;
        private final Deque<Page> pageStack;
        private final Deque<Fragment> fragmentStack;
        private final Deque<Layout> layoutStack;
        private final Deque<Integer> rendererStack;

        RenderingFlowTracker() {
            this.componentNamesStack = new ArrayDeque<>();
            this.pageStack = new ArrayDeque<>();
            this.fragmentStack = new ArrayDeque<>();
            this.layoutStack = new ArrayDeque<>();
            this.rendererStack = new ArrayDeque<>();
        }

        void start(Component component) {
            componentNamesStack.addLast(component.getName());
        }

        void in(Page page) {
            pageStack.addLast(page);
            rendererStack.addLast(TYPE_PAGE);
        }

        void in(Layout layout) {
            layoutStack.addLast(layout);
            componentNamesStack.addLast(NameUtils.getComponentName(layout.getName()));
            rendererStack.addLast(TYPE_LAYOUT);
        }

        void in(Fragment fragment) {
            fragmentStack.addLast(fragment);
            componentNamesStack.addLast(NameUtils.getComponentName(fragment.getName()));
            rendererStack.addLast(TYPE_FRAGMENT);
        }

        public String getCurrentComponentName() {
            return componentNamesStack.peekLast();
        }

        Optional<Page> getCurrentPage() {
            return Optional.ofNullable(pageStack.peekLast());
        }

        Optional<Fragment> getCurrentFragment() {
            return Optional.ofNullable(fragmentStack.peekLast());
        }

        Optional<Layout> getCurrentLayout() {
            return Optional.ofNullable(layoutStack.peekLast());
        }

        public boolean isInPage() {
            return rendererStack.peekLast().equals(TYPE_PAGE);
        }

        public boolean isInFragment() {
            return rendererStack.peekLast().equals(TYPE_FRAGMENT);
        }

        public boolean isInLayout() {
            return rendererStack.peekLast().equals(TYPE_LAYOUT);
        }

        void out(Page page) {
            if (!isInPage()) {
                throw new IllegalStateException("Not in a page");
            }
            pageStack.removeLast();
            rendererStack.removeLast();
        }

        void out(Layout layout) {
            if (!isInLayout()) {
                throw new IllegalStateException("Not in a layout");
            }
            layoutStack.removeLast();
            componentNamesStack.removeLast();
            rendererStack.removeLast();
        }

        void out(Fragment fragment) {
            if (!isInFragment()) {
                throw new IllegalStateException("Not in a fragment");
            }
            fragmentStack.removeLast();
            componentNamesStack.removeLast();
            rendererStack.removeLast();
        }

        void finish() {
            if (componentNamesStack.size() != 1) {
                throw new IllegalStateException("Not where you started");
            }
            componentNamesStack.removeLast();
        }
    }
}
