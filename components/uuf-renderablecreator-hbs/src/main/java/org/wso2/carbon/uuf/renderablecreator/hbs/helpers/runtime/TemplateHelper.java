/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime;

import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.FillPlaceholderHelper;

import java.io.IOException;

/**
 * Helper class to embed handlebars fragment template inside a <script> HTML tag and send it to client side.
 *
 * @since 1.0.0
 */
public class TemplateHelper extends FillPlaceholderHelper<String> {

    public static final String HELPER_NAME = "template";

    public TemplateHelper() {
        super(Placeholder.js);
    }

    @Override
    public CharSequence apply(String templateName, Options options) throws IOException {
        String scriptText;
        if (TagType.VAR.equals(options.tagType)) { // fragment template name is provided
            if (options.params.length < 1) {
                throw new UUFException("Fragment name is not given in the template helper.");
            }
            String fragmentName = options.param(0).toString();
            Lookup lookup = options.data(HbsRenderable.DATA_KEY_LOOKUP);
            RequestLookup requestLookup = options.data(HbsRenderable.DATA_KEY_REQUEST_LOOKUP);
            String componentName = requestLookup.tracker().getCurrentComponentName();
            Fragment fragment = lookup.getFragmentIn(componentName, fragmentName)
                    .orElseThrow(() -> new UUFException("Fragment '" + fragmentName + "' does not exist in " +
                            "component '" + componentName + "' or its dependencies."));
            if (!(fragment.getRenderable() instanceof HbsRenderable)) {
                throw new UUFException("The template of the fragment '" + fragmentName + "' is not a handlebars " +
                        "template.");
            }
            scriptText = "\n" + ((HbsRenderable) fragment.getRenderable()).getTemplate().text() + "\n";
        } else { // fragment is defined inline
            scriptText = options.fn.text();
        }

        String script = "<script id=\"" + templateName + "\" type=\"text/x-handlebars-template\">" + scriptText +
                "</script>";
        addToPlaceholder(script, options);
        return "";
    }
}
