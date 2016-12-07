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
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.FillPlaceholderHelper;

import java.io.IOException;
import java.util.Set;

public class CssHelper extends FillPlaceholderHelper<String> {

    public static final String HELPER_NAME = "css";

    public CssHelper() {
        super(Placeholder.css);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CharSequence apply(String relativePath, Options options) throws IOException {
        if (relativePath == null) {
            throw new IllegalArgumentException("Relative path of a CSS file cannot be null.");

        }

        StringBuilder completeRelativePath = new StringBuilder(relativePath);
        for (Object param : options.params) {
            completeRelativePath.append(param);
        }

        RequestLookup requestLookup = options.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP);

        String resourceIdentifier = getResourceIdentifier(requestLookup, completeRelativePath.toString());
        if (isResourceAlreadyResolved(resourceIdentifier, options)) {
            return "";
        }

        StringBuilder buffer = new StringBuilder("<link href=\"")
                .append(requestLookup.getPublicUri())
                .append('/')
                .append(completeRelativePath);
        buffer.append("\" rel=\"stylesheet\" type=\"text/css\" />\n");
        addToPlaceholder(buffer.toString(), options);
        ((Set) options.data(HbsRenderable.DATA_KEY_RESOLVED_RESOURCES)).add(resourceIdentifier);
        return "";
    }
}
