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

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.handlebars.PlaceholderWriter;

import java.io.IOException;

import static org.wso2.carbon.uuf.handlebars.renderable.HbsRenderable.DATA_KEY_CURRENT_WRITER;

public class DefinePlaceholderHelper implements Helper<String> {

    public static final String HELPER_NAME = "placeholder";

    @Override
    public CharSequence apply(String placeholderName, Options options) throws IOException {
        PlaceholderWriter writer = options.data(DATA_KEY_CURRENT_WRITER);
        if (options.tagType.inline()) {
            // {{placeholder "name"}}
            writer.addPlaceholder(placeholderName);
        } else {
            // {{#placeholder "name"}} default content of this placeholder {{/placeholder}}
            writer.addPlaceholder(placeholderName, options.fn().toString());
        }
        return "";
    }
}
