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
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents a Configuration object of a particular UUF component.
 */
public class Configuration extends HashMap<String, Object> {

    // TODO: 6/8/16 Cache values of 'appContext', 'theme', 'loginPageUri', 'menu', 'errorPages' configs
    public static final String KEY_APP_CONTEXT = "appContext";
    public static final String KEY_APP_CONTEXT_SERVER = "server";
    public static final String KEY_APP_CONTEXT_CLIENT = "client";
    public static final String KEY_THEME = "theme";
    public static final String KEY_LOGIN_PAGE_URI = "loginPageUri";
    public static final String KEY_MENU = "menu";
    public static final String KEY_ERROR_PAGES = "errorPages";

    public Configuration(Map<?, ?> rawMap) {
        this(rawMap.size());
        merge(rawMap);
    }

    private Configuration(int initialCapacity) {
        super(initialCapacity);
    }

    private Object getAppContext() {
        Object appContextObj = get(KEY_APP_CONTEXT);
        if (appContextObj == null) {
            return null;
        }
        if (!(appContextObj instanceof String) && !(appContextObj instanceof Map)) {
            throw new InvalidTypeException("Value of 'appContext' in the app configuration must be either a string " +
                                                   "or a Map<String, String>. Instead found '" +
                                                   appContextObj.getClass().getName() + "'.");
        }
        return appContextObj;
    }

    public String getServerAppContext() {
        Object appContextObj = getAppContext();
        if (appContextObj == null) {
            return null;
        }
        if (appContextObj instanceof String) {
            return (String) appContextObj;
        }
        // appContextObj must be a Map
        Map<?, ?> appContext = (Map) appContextObj;
        Object serverAppContextObj = appContext.get(KEY_APP_CONTEXT_SERVER);
        if (serverAppContextObj == null) {
            return null;
        }
        if (!(serverAppContextObj instanceof String)) {
            throw new InvalidTypeException(
                    "Value of 'server' in 'appContext' Map in the app configuration must be a string. Instead found '" +
                            serverAppContextObj.getClass().getName() + "'.");
        }
        return (String) serverAppContextObj;
    }

    public String getClientAppContext() {
        Object appContextObj = getAppContext();
        if (!(appContextObj instanceof Map)) {
            return null;
        }
        Map<?, ?> appContext = (Map) appContextObj;
        Object clientAppContextObj = appContext.get(KEY_APP_CONTEXT_CLIENT);
        if (clientAppContextObj == null) {
            return null;
        }
        if (!(clientAppContextObj instanceof String)) {
            throw new InvalidTypeException(
                    "Value of 'client' in 'appContext' Map in the app configuration must be a string. Instead found '" +
                            clientAppContextObj.getClass().getName() + "'.");
        }
        return (String) clientAppContextObj;
    }

    public String getThemeName() {
        Object themeNameObj = get(KEY_THEME);
        if (themeNameObj == null) {
            return null;
        }
        if (!(themeNameObj instanceof String)) {
            throw new InvalidTypeException(
                    "Value of 'theme' in the app configuration must be a string. Instead found '" +
                            themeNameObj.getClass().getName() + "'.");
        }
        String themeName = (String) themeNameObj;
        if (themeName.isEmpty()) {
            throw new IllegalArgumentException("Value of 'theme' in the app configuration cannot be empty.");
        }
        return themeName;
    }

    public String getLoginPageUri() {
        Object loginPageUriObj = get(KEY_LOGIN_PAGE_URI);
        if (loginPageUriObj == null) {
            return null;
        }
        if (!(loginPageUriObj instanceof String)) {
            throw new InvalidTypeException(
                    "Value of 'loginPageUri' in the app configuration must be a string. Instead found '" +
                            loginPageUriObj.getClass().getName() + "'.");
        }
        String loginPageUri = (String) loginPageUriObj;
        if (loginPageUri.isEmpty()) {
            throw new IllegalArgumentException("Value of 'loginPageUri' in the app configuration cannot be empty.");
        }
        if (loginPageUri.charAt(0) == '/') {
            throw new IllegalArgumentException(
                    "Value of 'loginPageUri' in the app configuration must start with a '/'. Instead found '" +
                            loginPageUri.charAt(0) + "' at the beginning.");
        }
        return loginPageUri;
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
        // Validate menu property.
        Object menuObj = super.get(KEY_MENU);
        if (menuObj == null) {
            return null;
        } else if (!(menuObj instanceof Map)) {
            throw new InvalidTypeException(
                    "Value of 'menu' in the configurations must be a Map<String, Map>. Instead found " +
                            menuObj.getClass().getName() + ".");
        }
        // Validate requested menu.
        Object menuMapObj = ((Map) menuObj).get(name);
        if (menuMapObj == null) {
            return null;
        } else if (!(menuMapObj instanceof Map)) {
            throw new InvalidTypeException("Menu '" + name + "' must be a Map<String, Map>. Instead found " +
                                                   menuObj.getClass().getName() + ".");
        }
        Map<?, ?> menuMap = (Map) menuMapObj;
        // Validate menu items.
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
        return (Map<String, Map>) menuMap;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getErrorPages() {
        Object errorPagesObj = get(KEY_ERROR_PAGES);
        if (errorPagesObj == null) {
            return Collections.<String, String>emptyMap();
        }
        if (errorPagesObj instanceof Map) {
            Map<?, ?> errorPagesMap = (Map) errorPagesObj;
            for (Entry<?, ?> entry : errorPagesMap.entrySet()) {
                if (!(entry.getKey() instanceof String)) {
                    throw new InvalidTypeException(
                            "Value of 'errorPages' in the app configuration must be a Map<String, String>." +
                                    " Instead found a '" + entry.getKey().getClass().getName() + "' key.");
                }
                if (!(entry.getValue() instanceof String)) {
                    throw new InvalidTypeException(
                            "Value of 'errorPages' in the app configuration must be a Map<String, String> " +
                                    "Instead found a '" + entry.getValue().getClass().getName() + "' value at key '" +
                                    entry.getKey() + "'.");
                }
            }
            return (Map<String, String>) errorPagesMap;
        } else {
            throw new InvalidTypeException(
                    "Value of 'errorPages' in the app configuration must be a Map<String, String>. " +
                            "Instead found '" + errorPagesObj.getClass().getName() + "'.");
        }
    }

    public String getAsString(String key) {
        Object value = super.get(key);
        if ((value == null) || (value instanceof String)) {
            return (String) value;
        } else {
            throw new InvalidTypeException(
                    "Value of '" + key + "' in the configuration must be a string. Instead found '" +
                            value.getClass().getName() + "'.");
        }
    }

    public String getAsStringOrDefault(String key, String defaultValue) {
        String value = getAsString(key);
        return (value == null) ? defaultValue : value;
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("Cannot change Configuration.");
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException("Cannot change Configuration.");
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        throw new UnsupportedOperationException("Cannot change Configuration.");
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("Cannot change Configuration.");
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException("Cannot change Configuration.");
    }

    @Override
    public Object replace(String key, Object value) {
        throw new UnsupportedOperationException("Cannot change Configuration.");
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        throw new UnsupportedOperationException("Cannot change Configuration.");
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
        throw new UnsupportedOperationException("Cannot change Configuration.");
    }

    @Override
    public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        throw new UnsupportedOperationException("Cannot change Configuration.");
    }

    @Override
    public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        throw new UnsupportedOperationException("Cannot change Configuration.");
    }

    @Override
    public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
        throw new UnsupportedOperationException("Cannot change Configuration.");
    }

    @Override
    public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        throw new UnsupportedOperationException("Cannot change Configuration.");
    }

    public void merge(Map<?, ?> rawMap) {
        for (Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() instanceof String) {
                super.compute((String) entry.getKey(), (key, oldValue) -> {
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
    private static Map deepMergeMap(Map oldMap, Map newMap) {
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
    private static List deepMergeList(List oldList, List newList) {
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

    public static Configuration emptyConfiguration() {
        return new Configuration(0);
    }
}
