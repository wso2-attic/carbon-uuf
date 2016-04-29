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

package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.handlebars.helpers.FillPlaceholderHelper;

import java.io.IOException;
import java.util.Optional;

import static org.wso2.carbon.uuf.handlebars.HbsRenderable.DATA_KEY_REQUEST_LOOKUP;

public class HeaderTitleHelper extends FillPlaceholderHelper {
    public static final String HELPER_NAME = "headerTitle";

    public HeaderTitleHelper() {
        super(HELPER_NAME);
    }

    public CharSequence apply(String title, Options options) throws IOException {
        RequestLookup requestLookup = options.data(DATA_KEY_REQUEST_LOOKUP);
        Optional<String> currentTitle = requestLookup.getPlaceholderContent(placeholderName);
        if (currentTitle.isPresent()) {
            throw new IllegalStateException(
                    "Cannot set header title. It is already set to '" + currentTitle.get() + "'.");
        }
        requestLookup.addToPlaceholder(HELPER_NAME, title);
        return "";
    }
}
