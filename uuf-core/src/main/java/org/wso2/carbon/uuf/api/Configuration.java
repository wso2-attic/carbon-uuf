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

    public static final String KEY_APP_CONTEXT = "appContext";
    public static final String KEY_APP_CONTEXT_SERVER = "server";
    public static final String KEY_APP_CONTEXT_CLIENT = "client";
    public static final String KEY_THEME = "theme";
    public static final String KEY_LOGIN_PAGE_URI = "loginPageUri";
    public static final String KEY_MENU = "menu";
    public static final String KEY_ERROR_PAGES = "errorPages";

    public Configuration(Map<?, ?> rawMap) {
        this(rawMap.size());
        for (Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() instanceof String) {
                super.put((String) entry.getKey(), entry.getValue());
            } else {
                throw new InvalidTypeException("Configurations must be a Map<String, Object>. Instead found a '" +
                                                       entry.getKey().getClass().getName() + "' key.");
            }
        }
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
     * with the given name, then an empty list is returned.
     *
     * @param name name of the menu
     * @return list of menu-items
     */
    @SuppressWarnings("unchecked")
    public List<Map> getMenu(String name) {
        // validate menu property
        Object menuObj = super.get(KEY_MENU);
        if (menuObj == null) {
            return Collections.emptyList();
        } else if (!(menuObj instanceof Map)) {
            throw new InvalidTypeException(
                    "Value of 'menu' in the configurations must be a Map<String, Object>. Instead found " +
                            menuObj.getClass().getName() + ".");
        }
        // validate requested menu
        Object menuListObj = ((Map) menuObj).get(name);
        if (menuListObj == null) {
            return Collections.emptyList();
        } else if (!(menuListObj instanceof List)) {
            throw new InvalidTypeException(
                    "Menu '" + name + "' must be a List<Map>. Instead found " + menuObj.getClass().getName() + ".");
        }
        List menuList = (List) menuListObj;
        // validate menu items
        for (int i = 0; i < menuList.size(); i++) {
            Object item = menuList.get(i);
            if (item == null) {
                throw new InvalidTypeException(
                        "Menu '" + name + "' must be a List<Map>. Instead found a null value at index " + i + ".");
            } else if (!(item instanceof Map)) {
                throw new InvalidTypeException(
                        "Menu '" + name + "' must be a List<Map>. Instead found a '" + item.getClass().getName() +
                                "' value at index " + i + ".");
            }
        }
        return (List<Map>) menuList;
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
                                    "Instead found a '" + entry.getValue().getClass().getName() + "' value.");
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

    public Object getAsStringOrDefault(String key, String defaultValue) {
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
        return super.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        throw new UnsupportedOperationException("Cannot change Configuration.");
    }

    public void merge(Configuration otherConfiguration) {
        for (Entry<String, Object> entry : otherConfiguration.entrySet()) {
            super.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    public static Configuration emptyConfiguration() {
        return new Configuration(0);
    }
}
