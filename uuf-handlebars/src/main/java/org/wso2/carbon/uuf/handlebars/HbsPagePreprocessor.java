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

package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.handlebars.helpers.init.LayoutHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class HbsPagePreprocessor {

    public static final String DATA_KEY_CURRENT_LAYOUT = HbsPagePreprocessor.class.getName() + "#layout";
    private static final Handlebars HANDLEBARS = new Handlebars();

    static {
        HANDLEBARS.registerHelper(LayoutHelper.HELPER_NAME, new LayoutHelper());
        HANDLEBARS.registerHelperMissing((context, options) -> "");
    }

    private final Optional<String> layout;

    public HbsPagePreprocessor(TemplateSource template) {
        String templatePath = template.filename();
        Context context = Context.newContext(Collections.emptyMap());
        try {
            HANDLEBARS.compile(template).apply(context);
        } catch (IOException e) {
            throw new UUFException(
                    "An error occurred when pre-processing the Handlebars template of page '" + templatePath + "'.", e);
        }
        layout = Optional.ofNullable(context.data(DATA_KEY_CURRENT_LAYOUT));
    }

    public Optional<String> getLayoutName() {
        return layout;
    }
}
