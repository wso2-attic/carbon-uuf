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

import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.FillPlaceholderHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;

import java.io.IOException;

public class FaviconHelper extends FillPlaceholderHelper<String> {

    public static final String HELPER_NAME = "favicon";

    public FaviconHelper() {
        super(Placeholder.favicon);
    }

    @Override
    public CharSequence apply(String relativePath, Options options) throws IOException {
        if (relativePath == null) {
            throw new IllegalArgumentException("Relative path of a favicon image cannot be null.");

        }

        RequestLookup requestLookup = options.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP);
        StringBuilder buffer = new StringBuilder("<link rel=\"shortcut icon\" href=\"")
                .append(requestLookup.getPublicUri())
                .append('/')
                .append(relativePath);
        for (Object param : options.params) {
            buffer.append(param);
        }
        String type = options.hash("type");
        if (type != null) {
            buffer.append("\" type=\"").append(type);
        }
        buffer.append("\" />\n");

        addToPlaceholder(buffer.toString(), options);
        return "";
    }
}
