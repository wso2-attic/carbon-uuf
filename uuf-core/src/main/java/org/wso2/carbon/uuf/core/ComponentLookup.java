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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.uuf.core.NameUtils.getFullyQualifiedName;
import static org.wso2.carbon.uuf.fileio.StaticResolver.DIR_NAME_COMPONENT_RESOURCES;
import static org.wso2.carbon.uuf.fileio.StaticResolver.STATIC_RESOURCE_URI_PREFIX;

public class ComponentLookup {

    private final String componentName;
    private final String componentContext;
    private final Map<String, Layout> allLayouts;
    private final SetMultimap<String, Fragment> allBindings;
    private final Map<String, Fragment> allFragments;
    private final Map<String, String> allPublicUriPrefixes;
    private final Map<String, ComponentLookup> immediateDependenciesLookups;
    private final Deque<String> componentsStack;
    private final Map<String, Object> allConfigurations;

    public ComponentLookup(String componentName, String componentContext, Set<Layout> layouts, Set<Fragment> fragments,
                           SetMultimap<String, Fragment> bindings, Map<String, Object> configurations,
                           Set<Component> dependencies) {
        if (componentName.isEmpty()) {
            throw new IllegalArgumentException("Component name cannot be empty.");
        }
        this.componentName = componentName;
        if (!componentContext.startsWith("/")) {
            throw new IllegalArgumentException("Context of a component must start with a '/'.");
        }
        this.componentContext = componentContext;

        this.allLayouts = layouts.stream().collect(Collectors.toMap(Layout::getName, l -> l));
        this.allFragments = fragments.stream().collect(Collectors.toMap(Fragment::getName, f -> f));
        this.allBindings = HashMultimap.create();
        for (Map.Entry<String, Fragment> entry : bindings.entries()) {
            this.allBindings.put(getFullyQualifiedName(componentName, entry.getKey()), entry.getValue());
        }
        this.allConfigurations = new HashMap<>();
        this.allPublicUriPrefixes = new HashMap<>();
        this.allPublicUriPrefixes.put(this.componentName, getPublicUriPrefix(this.componentContext));

        this.immediateDependenciesLookups = new HashMap<>();

        for (Component dependency : dependencies) {
            String dependencyComponentName = dependency.getName();
            ComponentLookup dependencyLookup = dependency.getLookup();
            this.immediateDependenciesLookups.put(dependencyComponentName, dependencyLookup);
            this.allLayouts.putAll(dependencyLookup.allLayouts);
            this.allFragments.putAll(dependencyLookup.allFragments);
            this.allBindings.putAll(dependencyLookup.allBindings);
            this.allConfigurations.putAll(dependencyLookup.allConfigurations);
            this.allPublicUriPrefixes.put(dependencyComponentName,
                                          getPublicUriPrefix(dependencyLookup.componentContext));
            this.allPublicUriPrefixes.putAll(dependencyLookup.allPublicUriPrefixes);
        }

        this.allConfigurations.putAll(configurations);
        this.componentsStack = new ArrayDeque<>(0);
    }

    private static String getPublicUriPrefix(String componentContext) {
        return STATIC_RESOURCE_URI_PREFIX + componentContext + "/";
    }

    private Optional<ComponentLookup> getComponentLookup(String componentName) {
        if (this.componentName.equals(componentName)) {
            // Found in this lookup.
            return Optional.of(this);
        }

        ComponentLookup immediateDependencyLookup = immediateDependenciesLookups.get(componentName);
        if (immediateDependencyLookup != null) {
            // Found in an immediate dependency.
            return Optional.of(immediateDependencyLookup);
        }

        // Could not found in immediate dependencies. Hence, do a depth-first search in dependencies.
        for (ComponentLookup dependencyLookup : immediateDependenciesLookups.values()) {
            Optional<ComponentLookup> componentLookup = dependencyLookup.getComponentLookup(componentName);
            if (componentLookup.isPresent()) {
                return componentLookup;
            }
        }

        // Could not find the lookup.
        return Optional.<ComponentLookup>empty();
    }

    public String getComponentName() {
        return componentName;
    }

    String getComponentContext() {
        return componentContext;
    }

    public Optional<Layout> getLayout(String layoutName) {
        return Optional.ofNullable(allLayouts.get(NameUtils.getFullyQualifiedName(componentName, layoutName)));
    }

    public Set<Fragment> getBindings(String zoneName) {
        String currentComponentName = getCurrentComponentName();
        Optional<ComponentLookup> currentComponentLookup = getComponentLookup(currentComponentName);
        if (currentComponentLookup.isPresent()) {
            return currentComponentLookup.get().allBindings.get(getFullyQualifiedName(currentComponentName, zoneName));
        } else {
            return Collections.<Fragment>emptySet();
        }
    }

    public Optional<Fragment> getFragment(String fragmentName) {
        String currentComponentName = getCurrentComponentName();
        return getComponentLookup(currentComponentName)
                .map(lookup -> lookup.allFragments.get(getFullyQualifiedName(currentComponentName, fragmentName)));
    }

    Map<String, Fragment> getFragments() {
        return allFragments;
    }

    public Map<String, Object> getConfigurations() {
        return allConfigurations;
    }

    String getPublicUriInfix(Page page) {
        return allPublicUriPrefixes.get(getCurrentComponentName()) + DIR_NAME_COMPONENT_RESOURCES;
    }

    String getPublicUriInfix(Layout layout) {
        return allPublicUriPrefixes.get(getCurrentComponentName()) + DIR_NAME_COMPONENT_RESOURCES;
    }

    String getPublicUriInfix(Fragment fragment) {
        return allPublicUriPrefixes.get(getCurrentComponentName()) + fragment.getSimpleName();
    }

    void in(Page page) {
        componentsStack.addLast(this.componentName);
    }

    void in(Fragment fragment) {
        componentsStack.addLast(NameUtils.getComponentName(fragment.getName()));
    }

    void in(Layout layout) {
        componentsStack.addLast(NameUtils.getComponentName(layout.getName()));
    }

    private String getCurrentComponentName() {
        return componentsStack.getLast();
    }

    String out() {
        return componentsStack.removeLast();
    }

    @Override
    public int hashCode() {
        return componentName.hashCode() + (31 * componentContext.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof ComponentLookup)) {
            ComponentLookup other = (ComponentLookup) obj;
            return componentName.equals(other.componentName) && componentContext.equals(other.componentContext);
        }
        return false;
    }
}
