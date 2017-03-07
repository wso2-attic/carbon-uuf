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

package org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

public class I18nHelper implements Helper<String> {

    public static final String HELPER_NAME = "i18n";
    private static final String DEFAULT_LOCALE = "en-us";
    private static final String LOCALE_HEADER = "Accept-Language";
    private static final String DATA_KEY_CURRENT_LOCALE = "CURRENT_LOCALE";

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        if (key == null) {
            throw new IllegalArgumentException("Key of a translating string cannot be null.");
        }

        RequestLookup requestLookup = options.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP);
        Lookup lookup = options.data(HbsRenderable.DATA_KEY_LOOKUP);

        // Check whether the current locale is already available in the options.
        Locale currentLocale = options.data(DATA_KEY_CURRENT_LOCALE);
        // If not available, get the current locale.
        if (currentLocale == null) {
            Object localeParam = options.hash.get("locale");
            if (localeParam != null) {
                currentLocale = Locale.forLanguageTag(localeParam.toString());
            } else {
                Object localeHeaderValue = requestLookup.getRequest().getHeaders().get(LOCALE_HEADER);
                currentLocale = lookup.getI18nResources().getLocale(localeHeaderValue.toString());
            }
            // Add the locale to the helper options. This will be used when evaluating this helper again in the same
            // request. This is done to increase the performance.
            options.data(DATA_KEY_CURRENT_LOCALE, currentLocale);
        }
        Properties props = lookup.getI18nResources().getI18nResource(currentLocale);
        if (props != null) {
            if (options.params.length == 0) {
                return props.getProperty(key, key);
            } else {
                MessageFormat format = new MessageFormat(props.getProperty(key, key), currentLocale);
                return format.format(options.params);
            }
        } else {
            return key;
        }
    }
}
