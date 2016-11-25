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

package org.wso2.carbon.uuf.renderablecreator.hbs.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;

import java.util.HashSet;
import java.util.Optional;

public abstract class FillPlaceholderHelper<T> implements Helper<T> {

    protected static final String RESOLVED_PLACEHOLDERS = "resolvedPlaceholders";
    private final Placeholder placeholder;

    protected FillPlaceholderHelper(Placeholder placeholder) {
        this.placeholder = placeholder;
    }

    public Placeholder getPlaceholder() {
        return placeholder;
    }

    protected void addToPlaceholder(String value, Options handlebarsOptions) {
        RequestLookup requestLookup = handlebarsOptions.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP);
        requestLookup.addToPlaceholder(placeholder, value);
    }

    protected Optional<String> getPlaceholderValue(Options handlebarsOptions) {
        RequestLookup requestLookup = handlebarsOptions.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP);
        return requestLookup.getPlaceholderContent(placeholder);
    }

    /**
     * Create a {@link HashSet} with the name {@code RESOLVED_PLACEHOLDERS} in handlebars options data if it's not
     * there. Check whether the given content is already resolved.
     *
     * @param options handlebars options to get {@code RESOLVED_PLACEHOLDERS}
     * @param content content to check for
     * @return <tt>true</tt> if the placeholder is already resolved, <tt>false</tt> otherwise
     */
    protected boolean isPlacedholderResolved(String content, Options options) {
        if (options.data(RESOLVED_PLACEHOLDERS) == null) {
            options.data(RESOLVED_PLACEHOLDERS, new HashSet<String>());
            return false;
        }
        return ((HashSet) options.data(RESOLVED_PLACEHOLDERS)).contains(content);
    }
}
