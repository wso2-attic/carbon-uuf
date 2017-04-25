/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.api.config;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import org.wso2.carbon.uuf.core.UriPatten;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Represents the final configuration of an UUF App.
 *
 * @since 1.0.0
 */
public class Configuration {

    private String contextPath;
    private String themeName;
    private String loginPageUri;
    private String sessionManager;
    private Map<Integer, String> errorPageUris;
    private String defaultErrorPageUri;
    private ListMultimap<String, MenuItem> menus;
    private Set<UriPatten> csrfIgnoreUris;
    private Set<UriPatten> xssIgnoreUris;
    private ResponseHeaders responseHeaders;
    private Map<String, Object> otherConfigurations;

    public Configuration() {
    }

    /**
     * Returns the configured client-side context path for the app.
     *
     * @return client-side context path
     */
    public Optional<String> getContextPath() {
        return Optional.ofNullable(contextPath);
    }

    /**
     * Sets the client-side context path for the app.
     *
     * @param contextPath client-side context path to be set
     * @throws IllegalArgumentException if the context path is empty or doesn't start with a '/'
     * @see #getContextPath()
     */
    public void setContextPath(String contextPath) {
        if (contextPath != null) {
            if (contextPath.isEmpty()) {
                throw new IllegalArgumentException("Context path cannot be empty.");
            } else if (contextPath.charAt(0) != '/') {
                throw new IllegalArgumentException("Context path must start with a '/'. Instead found '" +
                        contextPath.charAt(0) + "' at the beginning.");
            }
        }
        this.contextPath = contextPath;
    }

    /**
     * Returns the configured default theme name for the app.
     *
     * @return default theme name
     */
    public Optional<String> getThemeName() {
        return Optional.ofNullable(themeName);
    }

    /**
     * Sets the theme name for the app.
     *
     * @param themeName theme name to be set
     * @throws IllegalArgumentException if the theme name is empty
     * @see #getThemeName()
     */
    public void setThemeName(String themeName) {
        if ((themeName != null) && themeName.isEmpty()) {
            throw new IllegalArgumentException("Theme name cannot be empty.");
        }
        this.themeName = themeName;
    }

    /**
     * Returns the configured login page URI (without the app context path) for the app.
     *
     * @return login page's URI
     */
    public Optional<String> getLoginPageUri() {
        return Optional.ofNullable(loginPageUri);
    }

    /**
     * Sets the URI of the login page for the app.
     *
     * @param loginPageUri URI of the login page to be set
     * @throws IllegalArgumentException if login page URI is empty of doesn't start with a '/'
     * @see #getLoginPageUri()
     */
    public void setLoginPageUri(String loginPageUri) {
        if (loginPageUri != null) {
            if (loginPageUri.isEmpty()) {
                throw new IllegalArgumentException("Login page URI cannot be empty.");
            } else if (loginPageUri.charAt(0) != '/') {
                throw new IllegalArgumentException("Login page URI must start with a '/'. Instead found '" +
                        loginPageUri.charAt(0) + "' at the beginning.");
            }
        }
        this.loginPageUri = loginPageUri;
    }

    /**
     * Get session manager implementation class.
     *
     * @return session manager implementation class
     */
    public String getSessionManager() {
        return sessionManager;
    }

    /**
     * Set session manager implementation class.
     *
     * @param sessionManager session manager implementation class
     */
    public void setSessionManager(String sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Returns the configured error page URI (without the app context path) for the specified HTTP status code.
     *
     * @param httpStatusCode HTTP status code
     * @return corresponding error page's URI
     * @see #getDefaultErrorPageUri()
     */
    public Optional<String> getErrorPageUri(int httpStatusCode) {
        return Optional.ofNullable(errorPageUris.get(httpStatusCode));
    }

    /**
     * Sets the URIs of the error pages for the app.
     *
     * @param errorPageUris URIs of the error pages to be set
     * @throws IllegalArgumentException if HTTP status code is less than 100 or greater than 999
     * @throws IllegalArgumentException if an error page URI is empty or doesn't start with a '/'
     * @see #getErrorPageUri(int)
     * @see #setDefaultErrorPageUri(String)
     */
    public void setErrorPageUris(Map<Integer, String> errorPageUris) {
        if (errorPageUris == null) {
            this.errorPageUris = emptyMap();
        } else {
            for (Map.Entry<Integer, String> entry : errorPageUris.entrySet()) {
                int httpStatusCode = entry.getKey();
                String errorPageUri = entry.getValue();

                if ((httpStatusCode < 100) || (httpStatusCode > 999)) {
                    throw new IllegalArgumentException(
                            "HTTP status code of an error page entry must be between 100 and 999. Instead found '" +
                                    httpStatusCode + "' for URI '" + errorPageUri + "'.");
                }

                if (errorPageUri == null) {
                    throw new IllegalArgumentException(
                            "URI of an error page entry cannot be null. Found a null URI for HTTP status code '" +
                                    httpStatusCode + "'.");
                } else if (errorPageUri.isEmpty()) {
                    throw new IllegalArgumentException(
                            "URI of an error page entry cannot be empty. Found an empty URI for HTTP status code '" +
                                    httpStatusCode + "'.");
                } else if (errorPageUri.charAt(0) != '/') {
                    throw new IllegalArgumentException(
                            "URI of an error page entry must start with a '/'. Instead found '" +
                                    errorPageUri.charAt(0) + "' at the beginning of the URI for HTTP status code '" +
                                    httpStatusCode + "'.");
                }
            }
            this.errorPageUris = errorPageUris;
        }
    }

    /**
     * Return the configured default error page URI (without the app context path) for the app.
     *
     * @return default error page's URI
     * @see #getErrorPageUri(int)
     */
    public Optional<String> getDefaultErrorPageUri() {
        return Optional.ofNullable(defaultErrorPageUri);
    }

    /**
     * Sets the URI of the default error page for the app.
     *
     * @param defaultErrorPageUri URI of the default error page to be set
     * @throws IllegalArgumentException if default error page URI is empty for doesn't start with a '/'
     * @see #getDefaultErrorPageUri()
     * @see #setErrorPageUris(Map)
     */
    public void setDefaultErrorPageUri(String defaultErrorPageUri) {
        if (defaultErrorPageUri != null) {
            if (defaultErrorPageUri.isEmpty()) {
                throw new IllegalArgumentException("URI of the default error page cannot be empty.");
            } else if (defaultErrorPageUri.charAt(0) != '/') {
                throw new IllegalArgumentException(
                        "URI of the default error page must start with a '/'. Instead found '" +
                                defaultErrorPageUri.charAt(0) + "' at the beginning.");
            }
        }
        this.defaultErrorPageUri = defaultErrorPageUri;
    }

    /**
     * Returns the configured menu items for the given name.
     *
     * @param name name of the menu
     * @return menu items for the specified name
     */
    public List<MenuItem> getMenu(String name) {
        return menus.get(name);
    }

    /**
     * Sets menus for the app.
     *
     * @param menus menus to be set
     * @see #getMenu(String)
     */
    public void setMenus(List<? extends Menu> menus) {
        ImmutableListMultimap.Builder<String, MenuItem> builder = new ImmutableListMultimap.Builder<>();
        menus.forEach(menu -> builder.putAll(menu.getName(), menu.getItems()));
        this.menus = builder.build();
    }

    /**
     * Returns the list of URI's that doesn't require CSRF protection.
     *
     * @return set of URI's that doesn't require CSRF protection.
     */
    public Set<UriPatten> getCsrfIgnoreUris() {
        return csrfIgnoreUris;
    }

    /**
     * Sets the list of URI's that doesn't require CSRF protection.
     *
     * @param csrfIgnoreUris set of URI's that doesn't require CSRF protection.
     * @throws IllegalArgumentException if a URI is null or empty.
     */
    public void setCsrfIgnoreUris(Set<String> csrfIgnoreUris) {
        if (csrfIgnoreUris == null) {
            this.csrfIgnoreUris = emptySet();
        } else {
            Set<UriPatten> csrfIgnoreUriPatterns = new HashSet<>();
            for (String csrfUri : csrfIgnoreUris) {
                if (csrfUri == null) {
                    throw new IllegalArgumentException("CSRF ignore URI pattern cannot be null.");
                } else if (csrfUri.isEmpty()) {
                    throw new IllegalArgumentException("CSRF ignore URI pattern cannot be empty.");
                }
                try {
                    csrfIgnoreUriPatterns.add(new UriPatten(csrfUri));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("CSRF ignore URI pattern '" + csrfUri + "' is invalid.", e);
                }
            }
            this.csrfIgnoreUris = unmodifiableSet(csrfIgnoreUriPatterns);
        }
    }

    /**
     * Returns the list of URI's that doesn't require XSS protection.
     *
     * @return set of URI's that doesn't require XSS protection.
     */
    public Set<UriPatten> getXssIgnoreUris() {
        return xssIgnoreUris;
    }

    /**
     * Sets the list of URI's that doesn't require XSS protection.
     *
     * @param xssIgnoreUris set of URI's that doesn't require CSRF protection.
     * @throws IllegalArgumentException if a pattern is null or empty
     */
    public void setXssIgnoreUris(Set<String> xssIgnoreUris) {
        if (xssIgnoreUris == null) {
            this.xssIgnoreUris = emptySet();
        } else {
            Set<UriPatten> xssIgnoreUriPatterns = new HashSet<>();
            for (String xssUri : xssIgnoreUris) {
                if (xssUri == null) {
                    throw new IllegalArgumentException("XSS ignore URI pattern cannot be null.");
                } else if (xssUri.isEmpty()) {
                    throw new IllegalArgumentException("XSS ignore URI pattern cannot be empty.");
                }

                try {
                    xssIgnoreUriPatterns.add(new UriPatten(xssUri));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("XSS ignore URI pattern '" + xssUri + "' is invalid.", e);
                }
            }
            this.xssIgnoreUris = unmodifiableSet(xssIgnoreUriPatterns);
        }
    }

    /**
     * Returns the configured HTTP headers for the response in the security configuration.
     *
     * @return HTTP headers for the response
     */
    public ResponseHeaders getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(ResponseHeaders responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    /**
     * Return the business-logic related configurations that are configured for the app.
     *
     * @return business-logic related configurations
     */
    public Map<String, Object> other() {
        return otherConfigurations;
    }

    public void setOther(Map<String, Object> other) {
        this.otherConfigurations = (other == null) ? emptyMap() : unmodifiableMap(other);
    }

    /**
     * Bean class that represents security headers configurations in the app's config file of an UUF App.
     *
     * @since 1.0.0
     */
    public static class ResponseHeaders {
        private Map<String, String> staticResources = Collections.emptyMap();
        private Map<String, String> pages = Collections.emptyMap();

        public ResponseHeaders(Map<String, String> staticResources, Map<String, String> pages) {
            this.staticResources = staticResources;
            this.pages = pages;
        }

        /**
         * Returns HTTP response headers for static contents.
         *
         * @return HTTP response headers
         */
        public Map<String, String> getStaticResources() {
            return staticResources;
        }

        /**
         * Sets the HTTP response headers for static contents.
         *
         * @param staticResources HTTP response headers to be set
         */
        public void setStaticResources(Map<String, String> staticResources) {
            this.staticResources = staticResources;
        }

        /**
         * Returns HTTP response headers for pages.
         *
         * @return HTTP response headers
         */
        public Map<String, String> getPages() {
            return pages;
        }

        /**
         * Sets the HTTP response headers for pages.
         *
         * @param pages HTTP response headers to be set
         */
        public void setPages(Map<String, String> pages) {
            this.pages = pages;
        }
    }

    /**
     * Represents a configured menu for an UUF App.
     *
     * @since 1.0.0
     */
    public static class Menu {

        private final String name;
        private final List<MenuItem> items;

        /**
         * Creates a new menu.
         *
         * @param name  name of the menu
         * @param items menu items for the creating menu
         * @throws IllegalArgumentException if name is null or empty
         * @throws IllegalArgumentException if menu items is null or empty
         */
        public Menu(String name, List<MenuItem> items) {
            // Validate name.
            if (name == null) {
                throw new IllegalArgumentException("Name of a menu cannot be null.");
            } else if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of a menu cannot be empty.");
            } else {
                this.name = name;
            }
            // Validate menu items.
            if (items == null) {
                throw new IllegalArgumentException(
                        "Items of a menu cannot be null. Cannot find menu items for menu '" + name + "'.");
            } else if (items.isEmpty()) {
                throw new IllegalArgumentException(
                        "Items of a menu cannot be empty list. Cannot find menu items for menu '" + name + "'.");
            } else {
                this.items = Collections.unmodifiableList(items);
            }
        }

        /**
         * Returns the name of this menu.
         *
         * @return text of this menu
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the menu items of this menu.
         *
         * @return menu items of this menu
         */
        public List<MenuItem> getItems() {
            return items;
        }
    }

    /**
     * Represents a configured menu item for an UUF App.
     *
     * @since 1.0.0
     */
    public static class MenuItem {

        private final String text;
        private final String link;
        private final String icon;
        private final List<MenuItem> subMenus;

        /**
         * Creates a new menu item.
         *
         * @param text     text of the menu item
         * @param link     link of the menu item
         * @param icon     icon of the menu item
         * @param subMenus sub menus of the creating menu item
         * @throws IllegalArgumentException if text is null
         * @throws IllegalArgumentException if link is null
         */
        public MenuItem(String text, String link, String icon, List<MenuItem> subMenus) {
            // Validate text.
            if (text == null) {
                throw new IllegalArgumentException("Text of a menu item cannot be null.");
            } else {
                this.text = text;
            }
            // Validate link.
            if (link == null) {
                throw new IllegalArgumentException(
                        "Link of a menu item cannot be null. Cannot find link for menu item '" + text + "'.");
            } else {
                this.link = link;
            }
            this.icon = icon;
            this.subMenus = (subMenus == null) ? Collections.emptyList() : Collections.unmodifiableList(subMenus);
        }

        /**
         * Returns the text of this menu item.
         *
         * @return text of this menu item
         */
        public String getText() {
            return text;
        }

        /**
         * Returns the link of this menu item.
         *
         * @return link of this menu item.
         */
        public String getLink() {
            return link;
        }

        /**
         * Returns the icon CSS class of this menu item.
         *
         * @return icon CSS class of this menu item
         */
        public String getIcon() {
            return icon;
        }

        /**
         * Returns the sub-menus of this menu item.
         *
         * @return sub-menus of this menu item
         */
        public List<MenuItem> getSubMenus() {
            return subMenus;
        }
    }
}
