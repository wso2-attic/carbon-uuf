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
import java.util.ResourceBundle; 

import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.FillPlaceholderHelper;

import com.github.jknack.handlebars.Options;

public class I18nHelper extends FillPlaceholderHelper<String>{
	
    public static final String HELPER_NAME = "t";

	protected I18nHelper() {
		super(Placeholder.t);
	}

	@Override
	public CharSequence apply(String key, Options options) throws IOException {
        if (key == null) {
            throw new IllegalArgumentException("Key of a translating string cannot be null.");

        }		
        Locale currentLocale = ((RequestLookup) options.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP)).getLocale();
        //get string from locale file
        ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle", currentLocale);
        StringBuilder buffer = new StringBuilder(messages.getString(key));
        for (Object param : options.params) {
            buffer.append(param);
        }
        addToPlaceholder(buffer.toString(), options);
        return "";
	}

}
