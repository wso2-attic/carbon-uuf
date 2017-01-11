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

import com.google.common.collect.SetMultimap;
import org.wso2.carbon.uuf.api.config.Bindings;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.api.config.I18nResources;
import org.wso2.carbon.uuf.internal.util.NameUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Lookup {

    /**
     * All components in this lookup. key = fully qualified name (except for root component), value = component
     */
    private final Map<String, Component> components;
    /**
     * Flattened component dependencies. key = component fully qualified name, value = dependencies of the key
     */
    private final SetMultimap<String, String> flattenedDependencies;
    /**
     * All fragments in this lookup. key = fully qualified name , value = fragment
     */
    private final Map<String, Fragment> fragments;
    /**
     * All layouts of this lookup. key = fully qualified name, value = layout
     */
    private final Map<String, Layout> layouts;
    private final Configuration configuration;
    private final Bindings bindings;
    private final I18nResources i18nResources;

    public Lookup(Set<Component> components, SetMultimap<String, String> flattenedDependencies,
                  Configuration configuration, Bindings bindings, I18nResources i18nResources) {
        this.components = components.stream().collect(Collectors.toMap(Component::getName, component -> component));
        this.flattenedDependencies = flattenedDependencies;
        this.fragments = components.stream()
                .flatMap(component -> component.getFragments().stream())
                .collect(Collectors.toMap(Fragment::getName, fragment -> fragment));
        this.layouts = components.stream()
                .flatMap(component -> component.getLayouts().stream())
                .collect(Collectors.toMap(Layout::getName, layout -> layout));
        this.configuration = configuration;
        this.bindings = bindings;
        this.i18nResources = i18nResources;
    }

    public Optional<Component> getComponent(String componentName) {
        return Optional.ofNullable(components.get(componentName));
    }

    Map<String, Component> getAllComponents() {
        return components;
    }

    public Optional<Fragment> getFragmentIn(String componentName, String fragmentName) {
        if (NameUtils.isSimpleName(fragmentName)) {
            return Optional.ofNullable(fragments.get(NameUtils.getFullyQualifiedName(componentName, fragmentName)));
        }

        // fragmentName == <dependency-component-name>.<fragment-simple-name>
        String dependencyComponentName = NameUtils.getComponentName(fragmentName);
        if (componentName.equals(dependencyComponentName)) {
            // Fragment is referred with its fully qualified name even though it is in the same component.
            return Optional.ofNullable(fragments.get(fragmentName));
        }
        if (flattenedDependencies.get(componentName).contains(dependencyComponentName)) {
            // Component 'dependencyComponentName' is a dependency of component 'componentName'.
            return Optional.ofNullable(fragments.get(fragmentName));
        } else {
            // Component 'dependencyComponentName' is NOT a dependency of component 'componentName'.
            return Optional.<Fragment>empty();
        }
    }

    Map<String, Fragment> getAllFragments() {
        return fragments;
    }

    public List<Fragment> getBindings(String componentName, String zoneName) {
        return bindings.getBindings(NameUtils.getFullyQualifiedName(componentName, zoneName));
    }

    public Optional<Layout> getLayoutIn(String componentName, String layoutName) {
        if (NameUtils.isSimpleName(layoutName)) {
            return Optional.ofNullable(layouts.get(NameUtils.getFullyQualifiedName(componentName, layoutName)));
        }

        // layoutName == <dependency-component-name>.<layout-simple-name>
        String dependencyComponentName = NameUtils.getComponentName(layoutName);
        if (componentName.equals(dependencyComponentName)) {
            // Layout is referred with its fully qualified name even though it is in the same component.
            return Optional.ofNullable(layouts.get(layoutName));
        } else if (flattenedDependencies.get(componentName).contains(dependencyComponentName)) {
            // Component 'dependencyComponentName' is a dependency of component 'componentName'.
            return Optional.ofNullable(layouts.get(layoutName));
        } else {
            // Component 'dependencyComponentName' is NOT a dependency of component 'componentName'.
            return Optional.<Layout>empty();
        }
    }

    Map<String, Layout> getAllLayouts() {
        return layouts;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public I18nResources getI18nResources() {
        return i18nResources;
    }
}
