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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.model.ContextModel;
import org.wso2.carbon.uuf.renderablecreator.hbs.renderable.HbsRenderable;
import org.wso2.carbon.uuf.spi.model.Model;

import java.io.IOException;
import java.util.Optional;

public class FragmentHelper implements Helper<String> {

    public static final String HELPER_NAME = "fragment";
    private static final Logger log = LoggerFactory.getLogger(FragmentHelper.class);

    @Override
    public CharSequence apply(String fragmentName, Options options) throws IOException {
        if ((fragmentName == null) || fragmentName.isEmpty()) {
            throw new IllegalArgumentException("Fragment name cannot be null or empty.");
        }

        Lookup lookup = options.data(HbsRenderable.DATA_KEY_LOOKUP);
        RequestLookup requestLookup = options.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP);
        Optional<Fragment> fragment = lookup.getFragmentIn(requestLookup.tracker().getCurrentComponentName(),
                                                           fragmentName);
        if (!fragment.isPresent()) {
            throw new IllegalArgumentException(
                    "Fragment '" + fragmentName + "' does not exists in Component '" +
                            requestLookup.tracker().getCurrentComponentName() + "' or in its dependencies.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Fragment \"" + fragment.get() + "\" is called from '" + options.fn.text() + "'.");
        }

        Model model = new ContextModel(options.context, options.hash);
        API api = options.data(HbsRenderable.DATA_KEY_API);
        String content = fragment.get().render(model, lookup, requestLookup, api);
        return new Handlebars.SafeString(content);
    }
}
