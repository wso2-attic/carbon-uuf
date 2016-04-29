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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.uuf.fileio.StaticResolver.DIR_NAME_COMPONENT_RESOURCES;
import static org.wso2.carbon.uuf.fileio.StaticResolver.STATIC_RESOURCE_URI_PREFIX;

public class ComponentLookup {

    private final String componentName;
    private final String componentContext;
    private final Map<String, String> allComponentsContexts;
    private final Map<String, Layout> layouts;
    private final SetMultimap<String, Fragment> bindings;
    private final Map<String, Fragment> fragments;

    public ComponentLookup(String componentName, String componentContext, Set<Layout> layouts, Set<Fragment> fragments,
                           SetMultimap<String, Fragment> bindings, Set<Component> dependencies) {
        if (componentName.isEmpty()) {
            throw new IllegalArgumentException("Component name cannot be empty.");
        }
        this.componentName = componentName;
        if (!componentContext.startsWith("/")) {
            throw new IllegalArgumentException("Context of a component must start with a '/'.");
        }
        this.componentContext = componentContext;
        this.allComponentsContexts = new HashMap<>();
        this.allComponentsContexts.put(componentName, componentContext);

        this.layouts = layouts.stream().collect(Collectors.toMap(Layout::getName, l -> l));

        this.fragments = fragments.stream().collect(Collectors.toMap(Fragment::getName, f -> f));
        this.bindings = HashMultimap.create();
        for (Map.Entry<String, Fragment> entry : bindings.entries()) {
            this.bindings.put(NameUtils.getFullyQualifiedName(componentName, entry.getKey()), entry.getValue());
        }

        for (Component dependency : dependencies) {
            ComponentLookup dependencyLookup = dependency.getLookup();
            this.allComponentsContexts.put(dependencyLookup.componentName, dependencyLookup.componentContext);
            this.allComponentsContexts.putAll(dependencyLookup.allComponentsContexts);
            this.layouts.putAll(dependencyLookup.layouts);
            this.fragments.putAll(dependencyLookup.fragments);
            this.bindings.putAll(dependencyLookup.bindings);
        }
    }

    public String getComponentName() {
        return componentName;
    }

    String getComponentContext() {
        return componentContext;
    }

    public Optional<Layout> getLayout(String layoutName) {
        return Optional.ofNullable(layouts.get(NameUtils.getFullyQualifiedName(componentName, layoutName)));
    }

    public Set<Fragment> getBindings(String zoneName) {
        return bindings.get(NameUtils.getFullyQualifiedName(componentName, zoneName));
    }

    public Optional<Fragment> getFragment(String fragmentName) {
        return Optional.ofNullable(fragments.get(NameUtils.getFullyQualifiedName(componentName, fragmentName)));
    }

    Map<String, Fragment> getFragments() {
        return fragments;
    }

    String getPublicUriInfix(Page page) {
        return STATIC_RESOURCE_URI_PREFIX + componentContext + "/" + DIR_NAME_COMPONENT_RESOURCES;
    }

    String getPublicUriInfix(Layout layout) {
        String componentName = NameUtils.getComponentName(layout.getName());
        return STATIC_RESOURCE_URI_PREFIX + allComponentsContexts.get(componentName) + "/" +
                DIR_NAME_COMPONENT_RESOURCES;
    }

    String getPublicUriInfix(Fragment fragment) {
        String componentName = NameUtils.getComponentName(fragment.getName());
        return STATIC_RESOURCE_URI_PREFIX + allComponentsContexts.get(componentName) + "/" + fragment.getSimpleName();
    }
}
