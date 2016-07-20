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

import java.io.IOException;

import java.util.Locale;
import java.util.Properties;

import com.github.jknack.handlebars.Helper;

import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;

import com.github.jknack.handlebars.Options;

public class I18nHelper implements Helper<String> {
	
    public static final String HELPER_NAME = "t";
    private static final String DEFAULT_LOCALE = "en-US";
    private final String localeHeader = "Accept-Language";

	@Override
	public CharSequence apply(String key, Options options) throws IOException {
        if (key == null) {
            throw new IllegalArgumentException("Key of a translating string cannot be null.");

        }
        RequestLookup requestLookup = options.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP);

        Object localParam = options.hash.get("locale");
        Object localeRequest= requestLookup.getRequest().getHeaders().get(localeHeader).split(",")[0];
        Locale currentLocale;

        if(localParam != null){
            //Locale.Builder().setLanguageTag expects locale to be in "en-US" format (Note: - instead of _)
            //But enabling users to provide en_US is more intuitive hence using following approach
            String[] localeArry = localParam.toString().split("_");
            currentLocale  = new Locale(localeArry[0], localeArry[1]);
        } else if (localeRequest != null){
            currentLocale = new Locale.Builder().setLanguageTag(localeRequest.toString()).build();
        } else {
            currentLocale = new Locale.Builder().setLanguageTag(DEFAULT_LOCALE).build();
        }

        Lookup lookup = options.data(HbsRenderable.DATA_KEY_LOOKUP);
        Properties props = lookup.getAllI18nResources().get(currentLocale.toString());

        String value = props != null? props.getProperty(key, key) : key;
        StringBuilder buffer = new StringBuilder(value);

        for (Object param : options.params) {
            buffer.append(param);
        }
        //addToPlaceholder(buffer.toString(), options);
        return buffer.toString();
	}

}
