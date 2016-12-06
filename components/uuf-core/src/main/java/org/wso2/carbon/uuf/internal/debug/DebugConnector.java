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

package org.wso2.carbon.uuf.internal.debug;

import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Layout;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Theme;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class acts as a bridge between {@link Debugger} and {@link org.wso2.carbon.uuf.core.App}.
 */
public class DebugConnector {

    private final App app;

    /**
     * Creates a new debug connector for the specified app.
     *
     * @param connectingApp connecting app
     */
    public DebugConnector(App connectingApp) {
        this.app = connectingApp;
        // Since DebugConnector is instantiated per request, there is no point of pre-computing Components, Pages etc.
    }

    /**
     * Components of the connected App.
     *
     * @return components of the connected App
     */
    public Set<Component> getComponents() {
        return new HashSet<>(app.getComponents().values());
    }

    /**
     * Pages in the connected App.
     *
     * @return pages in the connected App
     */
    public Map<String, Set<Page>> getPages() {
        return app.getComponents().values().stream().collect(Collectors.toMap(Component::getContextPath,
                                                                              Component::getPages));
    }

    /**
     * Layouts in the connected app.
     *
     * @return layouts in the connected App
     */
    public Set<Layout> getLayouts() {
        return app.getLayouts().values().stream().collect(Collectors.toSet());
    }

    /**
     * Fragments in the connected App.
     *
     * @return fragments in the connected App
     */
    public Set<Fragment> getFragments() {
        return app.getFragments().values().stream().collect(Collectors.toSet());
    }

    /**
     * Themes of the connected App.
     *
     * @return themes of the connected App
     */
    public Set<Theme> getThemes() {
        return app.getThemes().values().stream().collect(Collectors.toSet());
    }

    /**
     * Configuration of the connected App.
     *
     * @return configuration of the connected App
     */
    public Configuration getConfiguration() {
        return app.getConfiguration();
    }
}
