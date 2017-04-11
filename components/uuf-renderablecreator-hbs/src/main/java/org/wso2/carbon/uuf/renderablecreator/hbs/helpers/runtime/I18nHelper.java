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
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.api.config.I18nResources;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;
import org.wso2.carbon.uuf.spi.HttpRequest;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class I18nHelper implements Helper<String> {

    public static final String HELPER_NAME = "i18n";

    private static final Locale FALLBACK_LOCALE = Locale.ENGLISH;
    private static final String DATA_KEY_CURRENT_REQUEST_LOCALE = "CURRENT_LOCALE";

    @Override
    public CharSequence apply(String key, Options options) throws IOException {
        if (key == null) {
            throw new IllegalArgumentException("Key of a translating string cannot be null.");
        }

        Locale locale;
        Lookup lookup = options.data(HbsRenderable.DATA_KEY_LOOKUP);

        // First priority is given to the passed locale parameter. {{i18n "key" locale="en-US"}}
        locale = computeLocale(options.hash);
        if (locale == null) {
            // Check whether we have already computed the locale for this request.
            Locale currentRequestLocale = options.data(DATA_KEY_CURRENT_REQUEST_LOCALE);
            if (currentRequestLocale == null) {
                // Second priority is given to the accept language header of the request.
                RequestLookup requestLookup = options.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP);
                locale = computeLocale(requestLookup.getRequest(), lookup.getI18nResources());
                if (locale == null) {
                    // Seems like we have failed to compute a locale in above approaches.
                    // Let's check whether a default locale is configured in the configuration.
                    locale = computeLocale(lookup.getConfiguration());
                    if (locale == null) {
                        // Since there is no other option, we choose fallback locale.
                        locale = FALLBACK_LOCALE;
                    }
                }

                options.data(DATA_KEY_CURRENT_REQUEST_LOCALE, locale);
            } else {
                locale = currentRequestLocale;
            }
        }

        return lookup.getI18nResources().getMessage(locale, key, options.params, key);
    }

    private static Locale computeLocale(Map<String, Object> hashParams) {
        Object localeParam = hashParams.get("locale");
        if ((localeParam instanceof String) && !localeParam.toString().isEmpty()) {
            return Locale.forLanguageTag(localeParam.toString());
        } else {
            return null;
        }
    }

    private static Locale computeLocale(HttpRequest request, I18nResources i18nResources) {
        String headerLocale = request.getHeaders().get(HttpRequest.HEADER_ACCEPT_LANGUAGE);
        return i18nResources.getLocale(headerLocale);
    }

    private static Locale computeLocale(Configuration configuration) {
        Object defaultLocale = configuration.other().get("defaultLocale");
        if ((defaultLocale instanceof String) && !defaultLocale.toString().isEmpty()) {
            return Locale.forLanguageTag(defaultLocale.toString());
        } else {
            return null;
        }
    }
}
