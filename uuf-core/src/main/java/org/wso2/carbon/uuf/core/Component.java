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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.spi.model.Model;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

public class Component {

    public static final String ROOT_COMPONENT_SIMPLE_NAME = "root";
    public static final String ROOT_COMPONENT_CONTEXT = "/root";
    private static final Logger log = LoggerFactory.getLogger(Component.class);

    private final String name;
    private final String version;
    private final String context;
    private final SortedSet<Page> pages;

    public Component(String name, String version, String context, SortedSet<Page> pages) {
        this.name = name;
        this.version = version;
        this.context = context;
        this.pages = pages;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getContext() {
        return context;
    }

    public Optional<String> renderPage(String pageUri, Model model, Lookup lookup, RequestLookup requestLookup,
                                       API api) {
        Page servingPage = null;
        for (Page page : pages) {
            Optional<Map<String, String>> pathParams = page.getUriPatten().match(pageUri);
            if (pathParams.isPresent()) {
                requestLookup.setPathParams(pathParams.get());
                servingPage = page;
                break;
            }
        }
        if (servingPage == null) {
            return Optional.<String>empty();
        }

        if (log.isDebugEnabled()) {
            log.debug("Component '" + name + "' is serving Page '" + servingPage + "' for URI '" + pageUri + "'.");
        }

        // Rendering flow tracking start.
        requestLookup.tracker().start(this);
        String html = servingPage.render(model, lookup, requestLookup, api);
        // Rendering flow tracking  finish.
        requestLookup.tracker().finish();
        return Optional.of(html);
    }

    public boolean hasPage(String pageUri) {
        return pages.stream().filter(page -> page.getUriPatten().matches(pageUri)).findFirst().isPresent();
    }

    public Set<Page> getPages() {
        return pages;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + (version.hashCode() * 31) + (context.hashCode() * 31);
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof Component)) {
            Component other = (Component) obj;
            return this.name.equals(other.name) && this.version.equals(other.version) &&
                    this.context.equals(other.context);
        }
        return false;
    }

    @Override
    public String toString() {
        return "{\"name\":\"" + name + "\", \"version\": \"" + version + "\", \"context\": \"" + context + "\"}";
    }
}
