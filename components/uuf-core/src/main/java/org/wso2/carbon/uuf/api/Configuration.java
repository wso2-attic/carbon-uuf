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

package org.wso2.carbon.uuf.api;

import org.wso2.carbon.uuf.exception.InvalidTypeException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a Configuration of a particular UUF App.
 */
public class Configuration {

    private static final String KEY_CONTEXT_PATH = "contextPath";
    private static final String KEY_THEME = "theme";
    private static final String KEY_LOGIN_PAGE_URI = "loginPageUri";
    private static final String KEY_MENU = "menu";
    private static final String KEY_ERROR_PAGES = "errorPages";

    private final Map<String, Object> map = new HashMap<>();
    private final Map<String, Object> unmodifiableMap = Collections.unmodifiableMap(map);
    // We cache following values to avoid extracting them from 'map' and validating them over and over again.
    private String cachedContextPath;
    private String cachedThemeName;
    private String cachedLoginPageUri;
    private Map<String, Map<String, Map>> cachedMenus = new ConcurrentHashMap<>();
    private Map<Integer, String> cachedErrorPageUris = new ConcurrentHashMap<>();
    private String cachedDefaultErrorPageUri;

    public Configuration() {
    }

    public Configuration(Map<?, ?> rawMap) {
        merge(rawMap);
    }

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
        if (loginPageUri.charAt(0) == '/') {
            throw new IllegalArgumentException(
                    "Value of 'loginPageUri' in the app configuration must start with a '/'. Instead found '" +
                            loginPageUri.charAt(0) + "' at the beginning.");
        }
        cachedLoginPageUri = loginPageUri; // Cache computed value.
        return Optional.of(loginPageUri);
    }

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
     * Returns the list of menu-items of the menu identified by the given {@code name}. If there is no menu associated
     * with the given name, then an {@code null} is returned.
     *
     * @param name name of the menu
     * @return menu-items
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

    public void merge(Map<?, ?> rawMap) {
        // TODO: 7/7/16 lock configuration after deploying the app
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() instanceof String) {
                map.compute((String) entry.getKey(), (key, oldValue) -> {
                    Object newValue = entry.getValue();
                    if (oldValue == null) {
                        return newValue; // Add the new value.
                    }

                    if (newValue instanceof Map && oldValue instanceof Map) {
                        return deepMergeMap((Map) oldValue, (Map) newValue);
                    } else if (newValue instanceof List && oldValue instanceof List) {
                        return deepMergeList((List) oldValue, (List) newValue);
                    }
                    return newValue; // Cannot merge if not a Map or a List, hence replace with the new value.
                });
            } else {
                throw new InvalidTypeException("Configurations must be a Map<String, Object>. Instead found a '" +
                                                       entry.getKey().getClass().getName() + "' key.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map deepMergeMap(Map oldMap, Map newMap) {
        for (Object key : newMap.keySet()) {
            Object newValueObj = newMap.get(key);
            Object oldValueObj = oldMap.get(key);
            if (oldValueObj instanceof Map && newValueObj instanceof Map) {
                oldMap.put(key, deepMergeMap((Map) oldValueObj, (Map) newValueObj));
            } else if (oldValueObj instanceof List && newValueObj instanceof List) {
                oldMap.put(key, deepMergeList((List) oldValueObj, (List) newValueObj));
            } else {
                oldMap.put(key, newValueObj);
            }
        }
        return oldMap;
    }

    @SuppressWarnings("unchecked")
    private List deepMergeList(List oldList, List newList) {
        for (Object newItemObj : newList) {
            int oldIndex = oldList.indexOf(newItemObj);
            if (oldIndex != -1) {
                Object oldItemObj = oldList.get(oldIndex);
                if (oldItemObj instanceof List && newItemObj instanceof List) {
                    oldList.set(oldIndex, deepMergeList((List) oldItemObj, (List) newItemObj));
                } else if (oldItemObj instanceof Map && newItemObj instanceof Map) {
                    oldList.set(oldIndex, deepMergeMap((Map) oldItemObj, (Map) newItemObj));
                } else {
                    oldList.set(oldIndex, newItemObj);
                }
            } else {
                oldList.add(newItemObj);
            }
        }
        return oldList;
    }

    /**
     * Returns this Configuration object as a Map.
     *
     * @return unmodifiable map
     */
    public Map<String, Object> asMap() {
        return unmodifiableMap;
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
