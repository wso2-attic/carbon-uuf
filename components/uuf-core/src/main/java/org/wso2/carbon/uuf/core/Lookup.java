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
import org.wso2.carbon.uuf.api.Configuration;
import org.wso2.carbon.uuf.internal.util.NameUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Properties;

public class Lookup {

    private final SetMultimap<String, String> flattenedDependencies;
    /**
     * All components in this lookup. key = fully qualified name (except for root component), value = component
     */
    private final Map<String, Component> components;
    /**
     * All fragments in this lookup. key = fully qualified name , value = fragment
     */
    private final Map<String, Fragment> fragments;
    /**
     * All bindings of this lookup. key = fully qualified name of the zone, value = pushed fragments set
     */
    private final SetMultimap<String, Fragment> bindings;
    /**
     * All layouts of this lookup. key = fully qualified name, value = layout
     */
    private final Map<String, Layout> layouts;
    private final Configuration configuration;
    private final Map<String, Properties> i18nResources;

    public Lookup(SetMultimap<String, String> flattenedDependencies) {
        this.flattenedDependencies = flattenedDependencies;
        this.components = new HashMap<>();
        this.layouts = new HashMap<>();
        this.fragments = new HashMap<>();
        this.bindings = HashMultimap.create();
        this.configuration = new Configuration();
        this.i18nResources = new HashMap<>();
    }

    public void add(Component component) {
        components.put(component.getName(), component);
    }

    public void add(Fragment fragment) {
        fragments.put(fragment.getName(), fragment);
    }

    public void add(Map<String, Properties> i18nConfiguration){
        for (Map.Entry<String, Properties> entry : i18nConfiguration.entrySet()) {
            Properties tmpProps = entry.getValue();
            Properties i18nProps = i18nResources.get(entry.getKey());
            if (!tmpProps.isEmpty()) {
                if (i18nProps == null) {
                    i18nResources.put(entry.getKey(), tmpProps);
                } else {
                    i18nProps.putAll(tmpProps);
                }
            }
        }
    }

    public void addBinding(String zoneName, Fragment fragment) {
        bindings.put(zoneName, fragment);
    }

    public void add(Layout layout) {
        layouts.put(layout.getName(), layout);
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

    public Set<Fragment> getBindings(String componentName, String zoneName) {
        return bindings.get(NameUtils.getFullyQualifiedName(componentName, zoneName));
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

    public Map<String, Properties> getAllI18nResources() {
        return i18nResources;
    }
}
