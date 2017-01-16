/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.spi.model.Model;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Component {

    public static final String ROOT_COMPONENT_CONTEXT_PATH = "/root";

    private final String name;
    private final String version;
    private final String contextPath;
    private final SortedSet<Page> pages;
    private final Set<Fragment> fragments;
    private final Set<Layout> layouts;
    private final Set<Component> dependencies;
    private final String path;

    public Component(String name, String version, String contextPath,
                     SortedSet<Page> pages, Set<Fragment> fragments, Set<Layout> layouts,
                     Set<Component> dependencies, String path) {
        this.name = name;
        this.version = version;
        this.contextPath = contextPath;
        this.pages = pages;
        this.fragments = fragments;
        this.layouts = layouts;
        this.dependencies = dependencies;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getContextPath() {
        return contextPath;
    }

    public SortedSet<Page> getPages() {
        return pages;
    }

    public Set<Fragment> getFragments() {
        return fragments;
    }

    public Set<Layout> getLayouts() {
        return layouts;
    }

    public Set<Component> getDependencies() {
        return dependencies;
    }

    /**
     * Returns all dependencies (including transitive ones) of this component.
     *
     * @return all dependencies of this component
     */
    Set<Component> getAllDependencies() {
        return Stream.concat(dependencies.stream(), // immediate dependencies
                             dependencies.stream().flatMap(dependency -> dependency.getAllDependencies().stream()))
                .collect(Collectors.toSet());
    }

    public String getPath() {
        return path;
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

    @Override
    public int hashCode() {
        return Objects.hash(name, version, path);
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof Component)) {
            Component other = (Component) obj;
            return Objects.equals(this.name, other.name) && Objects.equals(this.version, other.version) &&
                    Objects.equals(this.path, other.path);
        }
        return false;
    }

    @Override
    public String toString() {
        return "{\"name\":\"" + name + "\", \"version\": \"" + version + "\", \"contextPath\": \"" + contextPath +
                "\", \"path\": \"" + path + "\"}";
    }
}
