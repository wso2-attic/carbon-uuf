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

import org.wso2.carbon.uuf.exception.InvalidTypeException;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the Configuration of a particular UUF App.
 *
 * @since 1.0.0
 */
public class Configuration {

    /**
     * Configuration key to configure context path.
     * @see #getContextPath()
     */
    public static final String KEY_CONTEXT_PATH = "contextPath";
    /**
     * Configuration key to configure theme name.
     * @see #getThemeName()
     */
    public static final String KEY_THEME = "theme";
    /**
     * Configuration key to configure URI of the login page.
     * @see #getLoginPageUri()
     */
    public static final String KEY_LOGIN_PAGE_URI = "loginPageUri";
    /**
     * Configuration key to configure menus.
     * @see #getMenus()
     */
    public static final String KEY_MENU = "menu";
    /**
     * Configuration key to configure URIs of the error pages.
     * @see #getErrorPageUris()
     */
    public static final String KEY_ERROR_PAGES = "errorPages";

    private final Map<String, Object> map;
    private final Map<String, Object> unmodifiableMap;
    // We cache following values to avoid extracting them from 'map' and validating them over and over again.
    private String cachedContextPath;
    private String cachedThemeName;
    private String cachedLoginPageUri;
    private Map<String, Map<String, Map>> cachedMenus = new ConcurrentHashMap<>();
    private Map<Integer, String> cachedErrorPageUris = new ConcurrentHashMap<>();
    private String cachedDefaultErrorPageUri;

    /**
     * Creates a new {@link Configuration} instance wrapping the specified map.
     *
     * @param rawMap configuration map
     * @exception InvalidTypeException if the keys of the {@code rawMap} are not {@link String}s.
     */
    @SuppressWarnings("unchecked")
    public Configuration(Map<?, ?> rawMap) {
        // Validation
        rawMap.keySet().forEach(key -> {
            if (!(key instanceof String)) {
                throw new InvalidTypeException("Configurations must be a Map<String, Object>. Instead found a '" +
                                                       key.getClass().getName() + "' key.");
            }
        });
        this.map = (Map<String, Object>) rawMap;
        this.unmodifiableMap = Collections.unmodifiableMap(map);
    }

    /**
     * Returns the configured context path in this configuration. Context path should be configured with key {@link
     * #KEY_CONTEXT_PATH}.
     *
     * @return configured context path, or {@link Optional#empty()} if there is no value under key {@code
     * KEY_CONTEXT_PATH}
     * @exception InvalidTypeException     if configured value is not a {@link String}
     * @exception IllegalArgumentException if configured string is empty or does not start with a '/'
     */
    public Optional<String> getContextPath() {
        if (cachedContextPath != null) {
            return Optional.of(cachedContextPath); // Return cached value.
        }

        Object contextPathObj = map.get(KEY_CONTEXT_PATH);
        if (contextPathObj == null) {
            return Optional.<String>empty();
        } else if (!(contextPathObj instanceof String)) {
            throw new InvalidTypeException(
                    "Value of 'contextPath' in the app configuration must be a string. Instead found '" +
                            contextPathObj.getClass().getName() + "'.");
        }
        String contextPath = (String) contextPathObj;
        // Validate 'contextPath'
        if (contextPath.isEmpty()) {
            throw new IllegalArgumentException("Value of 'contextPath' in the app configuration cannot be empty.");
        } else if (contextPath.charAt(0) != '/') {
            throw new IllegalArgumentException("Value of 'contextPath' in the app configuration must start with '/'.");
        }
        cachedContextPath = contextPath; // Cache computed value.
        return Optional.of(contextPath);
    }

    /**
     * Returns the configured theme name in this configuration. Theme name should be configured with key {@link
     * #KEY_THEME}.
     *
     * @return configured theme name, or {@link Optional#empty()} if there is no value under key {@code KEY_THEME}
     * @exception InvalidTypeException     if configured value is not a {@link String}
     * @exception IllegalArgumentException if configured string is empty
     */
    public Optional<String> getThemeName() {
        if (cachedThemeName != null) {
            return Optional.of(cachedThemeName); // Return cached value.
        }

        Object themeNameObj = map.get(KEY_THEME);
        if (themeNameObj == null) {
            return Optional.<String>empty();
        } else if (!(themeNameObj instanceof String)) {
            throw new InvalidTypeException(
                    "Value of 'theme' in the app configuration must be a string. Instead found '" +
                            themeNameObj.getClass().getName() + "'.");
        }
        String themeName = (String) themeNameObj;
        // Validate 'themeName'
        if (themeName.isEmpty()) {
            throw new IllegalArgumentException("Value of 'theme' in the app configuration cannot be empty.");
        }
        cachedThemeName = themeName; // Cache computed value.
        return Optional.of(themeName);
    }

    /**
     * Returns the configured URI of the login page in this configuration. Login page URI should be configured with key
     * {@link #KEY_LOGIN_PAGE_URI}.
     *
     * @return configured login page URi, or {@link Optional#empty()} if there is no value under key {@code
     * KEY_LOGIN_PAGE_URI}
     * @exception InvalidTypeException     if configured value is not a {@link String}
     * @exception IllegalArgumentException if configured string is empty or does not start with a '/'
     */
    public Optional<String> getLoginPageUri() {
        if (cachedLoginPageUri != null) {
            return Optional.of(cachedLoginPageUri); // Return cached value.
        }

        Object loginPageUriObj = map.get(KEY_LOGIN_PAGE_URI);
        if (loginPageUriObj == null) {
            return Optional.<String>empty();
        } else if (!(loginPageUriObj instanceof String)) {
            throw new InvalidTypeException(
                    "Value of 'loginPageUri' in the app configuration must be a string. Instead found '" +
                            loginPageUriObj.getClass().getName() + "'.");
        }
        String loginPageUri = (String) loginPageUriObj;
        // Validate 'loginPageUri'
        if (loginPageUri.isEmpty()) {
            throw new IllegalArgumentException("Value of 'loginPageUri' in the app configuration cannot be empty.");
        }
        if (loginPageUri.charAt(0) != '/') {
            throw new IllegalArgumentException(
                    "Value of 'loginPageUri' in the app configuration must start with a '/'. Instead found '" +
                            loginPageUri.charAt(0) + "' at the beginning.");
        }
        cachedLoginPageUri = loginPageUri; // Cache computed value.
        return Optional.of(loginPageUri);
    }

    /**
     * Returns the configured menus in this configuration. Menus should be configured under the key {@link #KEY_MENU}.
     *
     * @return map of configured menus
     * @exception InvalidTypeException if configured value is not a {@code Map<String, Map<String, Map>}
     */
    @SuppressWarnings("unchecked")
    public Map<String, Map> getMenus() {
        Object menusMapObj = map.get(KEY_MENU);
        if (menusMapObj == null) {
            return Collections.<String, Map>emptyMap();
        } else if (!(menusMapObj instanceof Map)) {
            throw new InvalidTypeException(
                    "Value of 'menu' in the configurations must be a Map<String, Map<String, Map>. Instead found " +
                            menusMapObj.getClass().getName() + ".");
        }
        Map<?, ?> menusMap = (Map) menusMapObj;
        // Validate 'menusMap'
        for (Map.Entry<?, ?> entry : menusMap.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new InvalidTypeException(
                        "Value of 'menu' in the app configuration must be a Map<String, Map<String, Map>." +
                                "Instead found a '" + entry.getKey().getClass().getName() + "' key.");
            }
            if (!(entry.getValue() instanceof Map)) {
                throw new InvalidTypeException(
                        "Value of 'menu' in the app configuration must be a Map<String, Map<String, Map>. " +
                                "Instead found a '" + entry.getValue().getClass().getName() + "' value at key '" +
                                entry.getKey() + "'.");
            }
        }
        return (Map<String, Map>) menusMap;
    }

    /**
     * Returns the configured menu for the specified name.
     *
     * @param name menu name
     * @return configured error page URI or {@link Optional#empty()} if there is no value configured for the specified
     * HTTP code
     * @exception InvalidTypeException if configured menu with the specified name is not a {@code Map<String, Map>}
     * @see #getMenus()
     */
    @SuppressWarnings("unchecked")
    public Map<String, Map> getMenu(String name) {
        Map<String, Map> cachedMenu = cachedMenus.get(name);
        if (cachedMenu != null) {
            return cachedMenu; // Return cached value.
        }

        Map<?, ?> menuMap = getMenus().get(name);
        if (menuMap == null) {
            return Collections.<String, Map>emptyMap(); // Menu 'name' does not exits.
        }
        // Validate menu items in 'menuMap'
        for (Map.Entry entry : menuMap.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new InvalidTypeException("Menu '" + name + "' must be a Map<String, Map>. Instead found a '" +
                                                       entry.getKey().getClass().getName() + "' key.");
            }
            if (!(entry.getValue() instanceof Map)) {
                throw new InvalidTypeException("Menu '" + name + "' must be a Map<String, Map>. Instead found a '" +
                                                       entry.getValue().getClass().getName() + "' value at key '" +
                                                       entry.getKey() + "'.");
            }
        }
        Map<String, Map> menu = (Map<String, Map>) menuMap;
        cachedMenus.put(name, menu); // Cache computed value.
        return menu;
    }

    /**
     * Returns the configured URIs of error pages in this configuration. Error page URIs should be configured under the
     * key {@link #KEY_ERROR_PAGES}.
     *
     * @return map of configured error page URIs, where keys are the HTTP status code (or string "default") and values
     * are page URIs
     * @exception InvalidTypeException if configured value is not a {@code Map<String, String>}
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getErrorPageUris() {
        Object errorPagesObj = map.get(KEY_ERROR_PAGES);
        if (errorPagesObj == null) {
            return Collections.<String, String>emptyMap();
        } else if (!(errorPagesObj instanceof Map)) {
            throw new InvalidTypeException(
                    "Value of 'errorPages' in the app configuration must be a Map<String, String>. " +
                            "Instead found '" + errorPagesObj.getClass().getName() + "'.");
        }
        Map<?, ?> errorPagesMap = (Map) errorPagesObj;
        // Validate 'errorPagesMap'
        for (Map.Entry<?, ?> entry : errorPagesMap.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new InvalidTypeException(
                        "Value of 'errorPages' in the app configuration must be a Map<String, String>." +
                                " Instead found a '" + entry.getKey().getClass().getName() + "' key.");
            }
            if (!(entry.getValue() instanceof String)) {
                throw new InvalidTypeException(
                        "Value of 'errorPages' in the app configuration must be a Map<String, String>. " +
                                "Instead found a '" + entry.getValue().getClass().getName() + "' value at key '" +
                                entry.getKey() + "'.");
            }
        }
        return (Map<String, String>) errorPagesMap;
    }

    /**
     * Returns the configured error page URI for the specified HTTP status code.
     *
     * @param httpStatusCode HTTP status code
     * @return configured error page URI or {@link Optional#empty()} if there is no value configured for the specified
     * HTTP code
     * @exception IllegalArgumentException if configured error page URI is empty or does not start with a '/'
     * @see #getErrorPageUris()
     * @see #getDefaultErrorPageUri()
     */
    public Optional<String> getErrorPageUri(int httpStatusCode) {
        String cachedErrorPageUri = cachedErrorPageUris.get(httpStatusCode);
        if (cachedErrorPageUri != null) {
            return Optional.of(cachedErrorPageUri); // Return cached value.
        }
        String errorPageUri = getErrorPageUris().get(Integer.toString(httpStatusCode));
        if (errorPageUri == null) {
            return Optional.<String>empty();
        }
        // Validate 'errorPageUri'
        if (errorPageUri.isEmpty()) {
            throw new IllegalArgumentException("Error page URI for HTTP status code '" + httpStatusCode +
                                                       "' in the app configuration cannot be empty.");
        } else if (errorPageUri.charAt(0) != '/') {
            throw new IllegalArgumentException("Error page URI for HTTP status code '" + httpStatusCode +
                                                       "' in the app configuration must start with '/'.");
        }
        cachedErrorPageUris.put(httpStatusCode, errorPageUri); // Cache computed value.
        return Optional.of(errorPageUri);
    }

    /**
     * Returns the configured default error page URI.
     *
     * @return configured default error page URI or {@link Optional#empty()} if there is no value configured under key
     * "{@code default}"
     * @exception IllegalArgumentException if configured error page URI is empty or does not start with a '/'
     * @see #getErrorPageUris()
     * @see #getErrorPageUri(int)
     */
    public Optional<String> getDefaultErrorPageUri() {
        if (cachedDefaultErrorPageUri != null) {
            return Optional.of(cachedDefaultErrorPageUri); // Return cached value.
        }
        String defaultErrorPageUri = getErrorPageUris().get("default");
        if (defaultErrorPageUri == null) {
            return Optional.<String>empty();
        }
        // Validate 'defaultErrorPageUri'
        if (defaultErrorPageUri.isEmpty()) {
            throw new IllegalArgumentException("Default error page URI in the app configuration cannot be empty.");
        } else if (defaultErrorPageUri.charAt(0) != '/') {
            throw new IllegalArgumentException("Default error page URI in the app configuration must start with '/'.");
        }
        cachedDefaultErrorPageUri = defaultErrorPageUri; // Cache computed value.
        return Optional.of(defaultErrorPageUri);
    }

    /**
     * Returns this Configuration object as a Map.
     *
     * @return unmodifiable map
     */
    public Map<String, Object> asMap() {
        return unmodifiableMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return map.toString();
    }
}
