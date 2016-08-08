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

import org.wso2.carbon.uuf.api.Configuration;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.api.model.MapModel;
import org.wso2.carbon.uuf.exception.FragmentNotFoundException;
import org.wso2.carbon.uuf.exception.HttpErrorException;
import org.wso2.carbon.uuf.exception.PageNotFoundException;
import org.wso2.carbon.uuf.exception.PageRedirectException;
import org.wso2.carbon.uuf.exception.SessionNotFoundException;
import org.wso2.carbon.uuf.internal.core.auth.SessionRegistry;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.internal.util.UriUtils;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.model.Model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class App {

    private final String name;
    private final String contextPath;
    private final Lookup lookup;
    private final Map<String, Component> components;
    private final Component rootComponent;
    private final Map<String, Theme> themes;
    private final Theme defaultTheme;
    private final Configuration configuration;
    private final SessionRegistry sessionRegistry;

    public App(String name, String contextPath, Lookup lookup, Set<Theme> themes, SessionRegistry sessionRegistry) {
        this.name = name;
        this.contextPath = contextPath;
        this.lookup = lookup;
        this.configuration = this.lookup.getConfiguration();
        this.sessionRegistry = sessionRegistry;

        this.components = this.lookup.getAllComponents().values().stream()
                .collect(Collectors.toMap(Component::getContextPath, cmp -> cmp));
        this.rootComponent = this.components.get(Component.ROOT_COMPONENT_CONTEXT_PATH);

        this.themes = themes.stream().collect(Collectors.toMap(Theme::getName, theme -> theme));
        String configuredThemeName = this.configuration.getThemeName();
        if (configuredThemeName == null) {
            this.defaultTheme = null;
        } else {
            Theme configuredTheme = this.themes.get(configuredThemeName);
            if (configuredTheme == null) {
                throw new IllegalArgumentException("Theme '" + configuredThemeName + "' which is configured for app '" +
                                                           name + "' does not exists.");
            } else {
                this.defaultTheme = configuredTheme;
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getContextPath() {
        return contextPath;
    }

    public Map<String, Component> getComponents() {
        return components;
    }

    public Component getRootComponent() {
        return rootComponent;
    }

    public Map<String, Fragment> getFragments() {
        return lookup.getAllFragments();
    }

    public Map<String, Layout> getLayouts() {
        return lookup.getAllLayouts();
    }

    public Map<String, Theme> getThemes() {
        return themes;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * @param request  HTTP request
     * @param response HTTP response
     * @return rendered HTML output
     */
    public String renderPage(HttpRequest request, HttpResponse response) {
        RequestLookup requestLookup = createRequestLookup(request, response);
        API api = new API(sessionRegistry, requestLookup);
        // If exists, render Theme.
        Theme renderingTheme = getRenderingTheme(api);
        if (renderingTheme != null) {
            renderingTheme.render(requestLookup);
        }
        try {
            return renderPage(request.getUriWithoutContextPath(), null, requestLookup, api);
        } catch (SessionNotFoundException e) {
            String loginPageUri = configuration.getLoginPageUri();
            if (loginPageUri == null) {
                throw (HttpErrorException) e;
            } else {
                throw new PageRedirectException(loginPageUri);
            }
        }
    }

    public Optional<String> renderErrorPage(HttpErrorException ex, HttpRequest request, HttpResponse response) {
        Map<String, String> errorPages = configuration.getErrorPages();
        String errorPageUri = errorPages.getOrDefault(String.valueOf(ex.getHttpStatusCode()),
                                                      errorPages.get("default"));
        if (errorPageUri == null) {
            return Optional.<String>empty();
        }

        RequestLookup requestLookup = createRequestLookup(request, response);
        API api = new API(sessionRegistry, requestLookup);
        // If exists, render Theme.
        Theme renderingTheme = getRenderingTheme(api);
        if (renderingTheme != null) {
            renderingTheme.render(requestLookup);
        }
        // Create Model with HTTP status code and error message.
        Map<String, Object> modelMap = new HashMap<>(2);
        modelMap.put("status", ex.getHttpStatusCode());
        modelMap.put("message", ex.getMessage());
        return Optional.of(renderPage(errorPageUri, new MapModel(modelMap), requestLookup, api));
    }

    private String renderPage(String pageUri, Model model, RequestLookup requestLookup, API api) {
        // First try to render the page with 'root' component.
        Optional<String> output = rootComponent.renderPage(pageUri, model, lookup, requestLookup, api);
        if (output.isPresent()) {
            return output.get();
        }

        // Since 'root' component doesn't have the page, try with other components.
        int secondSlashIndex = pageUri.indexOf('/', 1);
        if (secondSlashIndex == -1) {
            // No component context found in the 'pageUri' URI.
            throw new PageNotFoundException("Requested page '" + pageUri + "' does not exists.");
        }
        String componentContext = pageUri.substring(0, secondSlashIndex);
        Component component = components.get(componentContext);
        if (component == null) {
            // No component found for the 'componentContext' key.
            throw new PageNotFoundException("Requested page '" + pageUri + "' does not exists.");
        }
        String uriWithoutComponentContext = pageUri.substring(secondSlashIndex);
        output = component.renderPage(uriWithoutComponentContext, model, lookup, requestLookup, api);
        if (output.isPresent()) {
            return output.get();
        }
        // No page found for 'uriWithoutComponentContext' in the 'component'.
        throw new PageNotFoundException("Requested page '" + pageUri + "' does not exists.");
    }

    /**
     * @param request  HTTP request
     * @param response HTTP response
     * @return rendered HTML output
     */
    public String renderFragment(HttpRequest request, HttpResponse response) {
        String uriWithoutContextPath = request.getUriWithoutContextPath();
        String uriPart = uriWithoutContextPath.substring(UriUtils.FRAGMENTS_URI_PREFIX.length());
        String fragmentName = NameUtils.getFullyQualifiedName(rootComponent.getName(), uriPart);
        // When building the dependency tree, all fragments are accumulated into the rootComponent.
        Fragment fragment = lookup.getAllFragments().get(fragmentName);
        if (fragment == null) {
            throw new FragmentNotFoundException("Requested fragment '" + fragmentName + "' does not exists.");
        }

        Model model = new MapModel(request.getQueryParams());
        RequestLookup requestLookup = createRequestLookup(request, response);
        API api = new API(sessionRegistry, requestLookup);
        return fragment.render(model, lookup, requestLookup, api);
    }

    public boolean hasPage(String uriWithoutContextPath) {
        // First check 'root' component has the page.
        if (rootComponent.hasPage(uriWithoutContextPath)) {
            return true;
        }

        // Since 'root' components doesn't have the page, search in other components.
        int secondSlashIndex = uriWithoutContextPath.indexOf('/', 1);
        if (secondSlashIndex == -1) {
            // No component context found in the 'uriWithoutContextPath' URI.
            return false;
        }
        String componentContext = uriWithoutContextPath.substring(0, secondSlashIndex);
        Component component = components.get(componentContext);
        if (component == null) {
            // No component found for the 'componentContext' key.
            return false;
        }
        String pageUri = uriWithoutContextPath.substring(secondSlashIndex);
        return component.hasPage(pageUri);
    }

    private Theme getRenderingTheme(API api) {
        Optional<String> sessionThemeName = api.getSession().map(Session::getThemeName);
        if (!sessionThemeName.isPresent()) {
            return defaultTheme;
        }
        return sessionThemeName
                .map(themes::get)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Theme '" + sessionThemeName.get() + "' which is set as for the current session of app '" +
                                name + "' does not exists."));
    }

    private RequestLookup createRequestLookup(HttpRequest request, HttpResponse response) {
        String clientContextPath = configuration.getContextPath();
        return new RequestLookup((clientContextPath == null ? contextPath : clientContextPath), request, response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, contextPath);
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"context\": \"" + contextPath + "\"}";
    }
}
