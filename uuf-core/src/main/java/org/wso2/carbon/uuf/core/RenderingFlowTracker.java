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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

class RenderingFlowTracker {

    private static final Integer TYPE_COMPONENT = 1;
    private static final Integer TYPE_PAGE = 2;
    private static final Integer TYPE_FRAGMENT = 3;
    private static final Integer TYPE_LAYOUT = 4;

    private final Deque<Component> componentStack;
    private final Deque<Page> pageStack;
    private final Deque<Fragment> fragmentStack;
    private final Deque<Layout> layoutStack;
    private final Deque<Integer> rendererStack;

    public RenderingFlowTracker() {
        this.componentStack = new ArrayDeque<>();
        this.pageStack = new ArrayDeque<>();
        this.fragmentStack = new ArrayDeque<>();
        this.layoutStack = new ArrayDeque<>();
        this.rendererStack = new ArrayDeque<>();
    }

    void in(Component component) {
        componentStack.push(component);
        rendererStack.push(TYPE_COMPONENT);
    }

    void in(Page page) {
        pageStack.push(page);
        rendererStack.push(TYPE_PAGE);
    }

    void in(Fragment fragment) {
        fragmentStack.push(fragment);
        rendererStack.push(TYPE_FRAGMENT);
    }

    void in(Layout layout) {
        layoutStack.push(layout);
        rendererStack.push(TYPE_LAYOUT);
    }

    Optional<Component> getCurrentComponent() {
        return Optional.ofNullable(componentStack.peekLast());
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

    boolean isInComponent() {
        return rendererStack.peekLast().equals(TYPE_COMPONENT);
    }

    boolean isInPage() {
        return rendererStack.peekLast().equals(TYPE_PAGE);
    }

    boolean isInFragment() {
        return rendererStack.peekLast().equals(TYPE_FRAGMENT);
    }

    boolean isInLayout() {
        return rendererStack.peekLast().equals(TYPE_LAYOUT);
    }

    void out(Component component) {
        if (!isInComponent()) {
            throw new IllegalStateException("Not in a component");
        }
        componentStack.removeLast();
        rendererStack.removeLast();
    }

    void out(Page page) {
        if (!isInPage()) {
            throw new IllegalStateException("Not in a page");
        }
        pageStack.removeLast();
        rendererStack.removeLast();
    }

    void out(Fragment fragment) {
        if (!isInFragment()) {
            throw new IllegalStateException("Not in a fragment");
        }
        fragmentStack.removeLast();
        rendererStack.removeLast();
    }

    void out(Layout layout) {
        if (!isInLayout()) {
            throw new IllegalStateException("Not in a layout");
        }
        layoutStack.removeLast();
        rendererStack.removeLast();
    }
}
