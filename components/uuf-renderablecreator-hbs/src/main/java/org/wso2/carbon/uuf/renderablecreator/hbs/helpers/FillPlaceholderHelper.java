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
import java.util.Set;

public abstract class FillPlaceholderHelper<T> implements Helper<T> {

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

    protected static boolean isResourceAlreadyResolved(String resourceRelativePath, Options handlebarsOptions) {
        RequestLookup requestLookup = handlebarsOptions.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP);
        RequestLookup.RenderingFlowTracker tracker = requestLookup.tracker();
        Set<String> resolvedResources = handlebarsOptions.data(HbsRenderable.DATA_KEY_RESOLVED_RESOURCES);

        // unique resource identifier is computed as fragment/component name+relativePath
        String resourceIdentifierPrefix = tracker.isInFragment() ? tracker.getCurrentFragment().get().getName()
                : tracker.getCurrentComponentName();
        String resourceIdentifier =  resourceIdentifierPrefix + resourceRelativePath;

        if (resolvedResources == null) {
            resolvedResources = new HashSet<>();
            handlebarsOptions.data(HbsRenderable.DATA_KEY_RESOLVED_RESOURCES, resolvedResources);
        }
        return !resolvedResources.add(resourceIdentifier);
    }

    protected static String concatParams(String firstParam, Object[] otherParams) {
        if ((otherParams == null) || (otherParams.length == 0)) {
            return firstParam;
        }

        StringBuilder builder = new StringBuilder(firstParam);
        for (Object param : otherParams) {
            builder.append(param);
        }
        return builder.toString();
    }
}
