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

import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.api.model.MapModel;
import org.wso2.carbon.uuf.exception.FragmentNotFoundException;
import org.wso2.carbon.uuf.exception.PageNotFoundException;
import org.wso2.carbon.uuf.internal.core.auth.SessionRegistry;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.internal.util.RequestUtil;
import org.wso2.carbon.uuf.spi.model.Model;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class App {

    private final String name;
    private final String context;
    private final Map<String, Component> components;
    private final Component rootComponent;
    private final Map<String, Theme> themes;
    private Theme appTheme;
    private final SessionRegistry sessionRegistry;

    public App(String name, Set<Component> components, Set<Theme> themes, SessionRegistry sessionRegistry) {
        this.name = name;

        this.components = components.stream().collect(Collectors.toMap(Component::getContext, cmp -> cmp));
        this.rootComponent = this.components.remove(Component.ROOT_COMPONENT_CONTEXT);
        this.context = this.rootComponent.getConfiguration().getAppContext()
                .orElse("/" + NameUtils.getSimpleName(name));

        this.themes = themes.stream().collect(Collectors.toMap(Theme::getName, theme -> theme));
        String defaultThemeName = this.rootComponent.getConfiguration().getDefaultThemeName();
        Theme defaultTheme = this.themes.get(defaultThemeName);
        if (defaultTheme == null) {
            throw new IllegalArgumentException(
                    "Theme '" + defaultThemeName + "' which is set as the default theme of app '" + name +
                            "' does not exists.");
        }
        this.appTheme = defaultTheme;

        this.sessionRegistry = sessionRegistry;
    }

    public String getName() {
        return name;
    }

    public String getContext() {
        return context;
    }

    public Map<String, Component> getComponents() {
        return components;
    }

    public Map<String, Theme> getThemes() {
        return themes;
    }

    public String renderPage(String uriWithoutAppContext, RequestLookup requestLookup) {
        API api = new API(sessionRegistry, requestLookup);
        getRenderingTheme(api).render(requestLookup);

        // First try to render the page with 'root' component.
        Optional<String> output = rootComponent.renderPage(uriWithoutAppContext, requestLookup, api);
        if (output.isPresent()) {
            updateAppTheme(api);
            return output.get();
        }

        // Since 'root' component doesn't have the page, try with other components.
        int secondSlashIndex = uriWithoutAppContext.indexOf('/', 1);
        if (secondSlashIndex == -1) {
            // No component context found in the 'uriWithoutAppContext' URI.
            throw new PageNotFoundException("Requested page '" + uriWithoutAppContext + "' does not exists.");
        }
        String componentContext = uriWithoutAppContext.substring(0, secondSlashIndex);
        Component component = components.get(componentContext);
        if (component == null) {
            // No component found for the 'componentContext' key.
            throw new PageNotFoundException("Requested page '" + uriWithoutAppContext + "' does not exists.");
        }
        String pageUri = uriWithoutAppContext.substring(secondSlashIndex);
        output = component.renderPage(pageUri, requestLookup, api);
        if (output.isPresent()) {
            updateAppTheme(api);
            return output.get();
        }
        // No page found for 'pageUri' in the 'component'.
        throw new PageNotFoundException("Requested page '" + uriWithoutAppContext + "' does not exists.");
    }

    /**
     * Returns rendered output of this fragment uri. This method intended to use for serving AJAX requests.
     *
     * @param uriWithoutAppContext fragment uri
     * @param requestLookup        request lookup
     * @return rendered output
     */
    public String renderFragment(String uriWithoutAppContext, RequestLookup requestLookup) {
        String fragmentName = uriWithoutAppContext.substring(RequestUtil.FRAGMENTS_URI_PREFIX.length());
        if (NameUtils.isSimpleName(fragmentName)) {
            fragmentName = NameUtils.getFullyQualifiedName(rootComponent.getName(), fragmentName);
        }
        // When building the dependency tree, all fragments are accumulated into the rootComponent.
        Fragment fragment = rootComponent.getAllFragments().get(fragmentName);
        if (fragment == null) {
            throw new FragmentNotFoundException("Requested fragment '" + fragmentName + "' does not exists.");
        }

        Model model = new MapModel(requestLookup.getRequest().getQueryParams().entrySet().stream()
                                           .collect(Collectors.toMap(Map.Entry::getKey, entry -> (Object) entry)));
        API api = new API(sessionRegistry, requestLookup);
        return fragment.render(model, rootComponent.getLookup(), requestLookup, api);
    }

    public boolean hasPage(String uriWithoutAppContext) {
        // First check 'root' component has the page.
        if (rootComponent.hasPage(uriWithoutAppContext)) {
            return true;
        }

        // Since 'root' components doesn't have the page, search in other components.
        int secondSlashIndex = uriWithoutAppContext.indexOf('/', 1);
        if (secondSlashIndex == -1) {
            // No component context found in the 'uriWithoutAppContext' URI.
            return false;
        }
        String componentContext = uriWithoutAppContext.substring(0, secondSlashIndex);
        Component component = components.get(componentContext);
        if (component == null) {
            // No component found for the 'componentContext' key.
            return false;
        }
        String pageUri = uriWithoutAppContext.substring(secondSlashIndex);
        return component.hasPage(pageUri);
    }

    private Theme getRenderingTheme(API api) {
        Session session = api.getSession();
        if (session == null) {
            return appTheme;
        }
        String sessionThemeName = session.getThemeName();
        if (sessionThemeName == null) {
            return appTheme;
        }
        Theme sessionTheme = themes.get(sessionThemeName);
        if (sessionTheme == null) {
            throw new IllegalArgumentException(
                    "Theme '" + sessionThemeName + "' which is set as for the current session does not exists.");
        }
        return sessionTheme;
    }

    private void updateAppTheme(API api) {
        String appThemeName = api.getAppTheme();
        if (appThemeName == null) {
            return; // Nothing to update.
        }
        Theme appTheme = themes.get(appThemeName);
        if (appTheme == null) {
            throw new IllegalArgumentException(
                    "Theme '" + appThemeName + "' which is set for the app '" + name + "' does not exists.");
        }
        this.appTheme = appTheme; // Update the theme of the app.
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"context\": \"" + context + "\"}";
    }
}
