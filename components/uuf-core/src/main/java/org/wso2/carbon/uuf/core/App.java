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

import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.api.model.MapModel;
import org.wso2.carbon.uuf.exception.FragmentNotFoundException;
import org.wso2.carbon.uuf.exception.HttpErrorException;
import org.wso2.carbon.uuf.exception.PageNotFoundException;
import org.wso2.carbon.uuf.exception.PageRedirectException;
import org.wso2.carbon.uuf.exception.SessionNotFoundException;
import org.wso2.carbon.uuf.exception.UUFException;
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
        this.defaultTheme = this.configuration.getThemeName()
                .map(configuredThemeName -> {
                    Theme configuredTheme = App.this.themes.get(configuredThemeName);
                    if (configuredTheme == null) {
                        throw new IllegalArgumentException(
                                "Theme '" + configuredThemeName + "' which is configured for app '" + name +
                                        "' does not exists. Available themes: " + themes);
                    }
                    return configuredTheme;
                }).orElse(null);
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
        Theme theme = getRenderingTheme(api);
        try {
            return renderPageUri(request.getUriWithoutContextPath(), null, requestLookup, api, theme);
        } catch (SessionNotFoundException e) {
            String loginPageUri = configuration.getLoginPageUri().orElseThrow(() -> e);
            // Redirect to the login page.
            throw new PageRedirectException(requestLookup.getContextPath() + loginPageUri, e);
        } catch (PageRedirectException e) {
            throw e;
        } catch (PageNotFoundException e) {
            // See https://googlewebmastercentral.blogspot.com/2010/04/to-slash-or-not-to-slash.html
            // If the tailing '/' is extra or a it is missing, then send 301 with corrected URL.
            String uriWithoutContextPath = request.getUriWithoutContextPath();
            String correctedUriWithoutContextPath = uriWithoutContextPath.endsWith("/") ?
                    uriWithoutContextPath.substring(0, uriWithoutContextPath.length() - 1) :
                    (uriWithoutContextPath + "/");
            if (hasPage(correctedUriWithoutContextPath)) {
                // Redirecting to the correct page.
                String correctedUri = request.getContextPath() + correctedUriWithoutContextPath;
                if (request.getQueryString() != null) {
                    correctedUri = correctedUri + '?' + request.getQueryString();
                }
                throw new PageRedirectException(correctedUri, e);
            } else {
                return renderErrorPage(e, requestLookup, api, theme);
            }
        } catch (HttpErrorException e) {
            return renderErrorPage(e, requestLookup, api, theme);
        } catch (UUFException e) {
            return renderErrorPage(new HttpErrorException(HttpResponse.STATUS_INTERNAL_SERVER_ERROR, e.getMessage(), e),
                                   requestLookup, api, theme);
        }
    }

    private String renderErrorPage(HttpErrorException e, RequestLookup requestLookup, API api, Theme theme) {
        String errorPageUri = configuration.getErrorPageUri(e.getHttpStatusCode())
                .orElse(configuration.getDefaultErrorPageUri().orElseThrow(() -> e));

        // Create Model with HTTP status code and error message.
        Map<String, Object> modelMap = new HashMap<>(2);
        modelMap.put("status", e.getHttpStatusCode());
        modelMap.put("message", e.getMessage());
        requestLookup.tracker().reset(); // reset rendering tracking
        return renderPageUri(errorPageUri, new MapModel(modelMap), requestLookup, api, theme);
    }

    private String renderPageUri(String pageUri, Model model, RequestLookup requestLookup, API api, Theme theme) {
        // If theme exists, add theme values to the requestLookup
        if (theme != null) {
            theme.addPlaceHolderValues(requestLookup);
        }
        // First try to addPlaceHolderValues the page with 'root' component.
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

    private boolean hasPage(String uriWithoutContextPath) {
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
        return new RequestLookup((configuration.getContextPath().orElse(contextPath)), request, response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, contextPath);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof App) && (this.name.equals(((App) obj).name));
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"context\": \"" + contextPath + "\"}";
    }
}
