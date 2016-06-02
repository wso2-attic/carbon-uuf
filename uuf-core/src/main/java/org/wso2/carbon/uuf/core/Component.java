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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.Configuration;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

public class Component {

    public static final String ROOT_COMPONENT_NAME = "root";
    public static final String ROOT_COMPONENT_CONTEXT = "/root";
    private static final Logger log = LoggerFactory.getLogger(Component.class);

    private final String name;
    private final String version;
    private final SortedSet<Page> pages;
    private final ComponentLookup lookup;

    public Component(String name, String version, SortedSet<Page> pages, ComponentLookup lookup) {
        if (!name.equals(lookup.getComponentName())) {
            throw new IllegalArgumentException("Specified 'lookup' does not belong to this component.");
        }
        this.name = name;
        this.version = version;
        this.pages = pages;
        this.lookup = lookup;
    }

    String getName() {
        return name;
    }

    String getContext() {
        return lookup.getComponentContext();
    }

    ComponentLookup getLookup() {
        return lookup;
    }

    Configuration getConfiguration() {
        return lookup.getConfigurations();
    }

    public Map<String, Fragment> getAllFragments() {
        return lookup.getAllFragments();
    }

    public Optional<Page> getPage(String pageUri) {
        return pages.stream().filter(page -> page.getUriPatten().matches(pageUri)).findFirst();
    }

    @Deprecated
    public Optional<String> renderPage(String pageUri, RequestLookup requestLookup, API api) {
        Page servingPage = null;
        for (Page page : pages) {
            Optional<Map<String, String>> uriParams = page.getUriPatten().match(pageUri);
            if (uriParams.isPresent()) {
                requestLookup.setUriParams(uriParams.get());
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

        return Optional.of(servingPage.render(null, lookup, requestLookup, api));
    }

    public boolean hasPage(String pageUri) {
        return getPage(pageUri).isPresent();
    }

    public Set<Page> getPages() {
        return pages;
    }

    @Override
    public String toString() {
        return "{\"name\":\"" + name + "\", \"version\": \"" + version + "\", \"context\": \"" + getContext() + "\"}";
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of hash tables such as those
     * provided by {@link java.util.HashMap}. This assumes name, version and context is immutable
     * (http://stackoverflow.com/a/27609/1560536).
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(version).append(getContext()).toHashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Component)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Component comp = (Component) obj;
        return new EqualsBuilder().
                append(name, comp.name).
                append(version, comp.version).
                append(getContext(), comp.getContext()).
                isEquals();
    }
}
