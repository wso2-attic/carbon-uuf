/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Holds the i18n language resources of an UUF App.
 *
 * @since 1.0.0
 */
public class I18nResources {

    private final SortedMap<String, Properties> i18nResources = new TreeMap<>();

    /**
     * Adds the given language.
     *
     * @param locale language to be add
     * @param i18n   properties
     */
    public void addI18nResource(Locale locale, Properties i18n) {
        // Convert the language key to lower case before adding to the map. This is done because various browsers
        // send the locale in different formats.
        Properties i18nResource = this.i18nResources.get(locale.toLanguageTag());
        if (i18nResource == null) {
            this.i18nResources.put(locale.toLanguageTag(), i18n);
        } else {
            i18nResource.putAll(i18n);
        }
    }

    /**
     * Returns available languages in this resources collection.
     *
     * @return available languages
     */
    public Set<String> getAvailableLanguages() {
        return i18nResources.keySet();
    }

    /**
     * Returns the i18n resource for the given language.
     *
     * @param locale Locale of the i18n resource
     * @return i18n resource
     */
    public Properties getI18nResource(Locale locale) {
        return i18nResources.get(locale.toLanguageTag());
    }
}
