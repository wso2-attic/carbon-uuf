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

package org.wso2.carbon.uuf.internal.debug;

import org.wso2.carbon.uuf.api.Configuration;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Layout;
import org.wso2.carbon.uuf.core.Theme;

import java.util.HashSet;
import java.util.Set;

/**
 * This class acts as a bridge between {@link Debugger} and {@link org.wso2.carbon.uuf.core.App}.
 */
public class DebugConnector {

    private Set<Component> components = new HashSet<>();
    private Set<Fragment> fragments = new HashSet<>();
    private Set<Theme> themes = new HashSet<>();
    private Set<Layout> layouts = new HashSet<>();
    private Configuration configuration;

    /**
     * Adding this component.
     *
     * @param component component object
     */
    public void addComponent(Component component) {
        this.components.add(component);
    }

    /**
     * Returns a set of components.
     *
     * @return components
     */
    public Set<Component> getComponents() {
        return components;
    }

    /**
     * Adding this fragment.
     *
     * @param fragment fragment object
     */
    public void addFragment(Fragment fragment) {
        this.fragments.add(fragment);
    }

    /**
     * Returns a set of components.
     *
     * @return components
     */
    public Set<Fragment> getFragments() {
        return fragments;
    }

    /**
     * Adding this theme
     *
     * @param theme theme object.
     */
    public void addTheme(Theme theme) {
        this.themes.add(theme);
    }

    /**
     * Returns a set of themes.
     *
     * @return a set of themes
     */
    public Set<Theme> getThemes() {
        return themes;
    }

    /**
     * Setting app configuration.
     *
     * @param configuration configuration object
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns app configuration.
     *
     * @return current app configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Adding this layout.
     *
     * @param layout layout object
     */
    public void addLayout(Layout layout) {
        this.layouts.add(layout);
    }

    /**
     * Returns set of layouts.
     *
     * @return a set of layouts
     */
    public Set<Layout> getLayouts() {
        return layouts;
    }
}
