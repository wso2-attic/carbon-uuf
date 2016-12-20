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

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.model.ContextModel;

import java.io.IOException;
import java.util.List;

public class DefineZoneHelper implements Helper<String> {

    public static final String HELPER_NAME = "defineZone";

    @Override
    public CharSequence apply(String zoneName, Options options) throws IOException {
        if ((zoneName == null) || zoneName.isEmpty()) {
            throw new IllegalArgumentException("Zone name cannot be null or empty.");
        }

        Lookup lookup = options.data(HbsRenderable.DATA_KEY_LOOKUP);
        RequestLookup requestLookup = options.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP);
        StringBuilder buffer = new StringBuilder();

        List<Fragment> bindings = lookup.getBindings(requestLookup.tracker().getCurrentComponentName(), zoneName);
        if (!bindings.isEmpty()) {
            API api = options.data(HbsRenderable.DATA_KEY_API);
            for (Fragment fragment : bindings) {
                buffer.append(fragment.render(new ContextModel(options.context), lookup, requestLookup, api));
            }
        }

        requestLookup.getZoneContent(zoneName).ifPresent(buffer::append);
        return new Handlebars.SafeString(buffer.toString());
    }
}
