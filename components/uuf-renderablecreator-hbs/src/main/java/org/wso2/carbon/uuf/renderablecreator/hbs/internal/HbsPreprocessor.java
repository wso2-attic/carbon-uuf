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

package org.wso2.carbon.uuf.renderablecreator.hbs.internal;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.api.auth.Permission;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.registry.InitHelperRegistry;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class HbsPreprocessor {

    public static final String DATA_KEY_CURRENT_LAYOUT = HbsPreprocessor.class.getName() + "#layout";
    public static final String DATA_KEY_SECURED = HbsPreprocessor.class.getName() + "#secured";
    private static final Handlebars HANDLEBARS = new Handlebars().with(new InitHelperRegistry());

    private final Optional<String> layout;
    private final Permission permission;

    public HbsPreprocessor(TemplateSource template) {
        String templatePath = template.filename();
        Context context = Context.newContext(Collections.emptyMap());
        try {
            HANDLEBARS.compile(template).apply(context);
        } catch (IOException e) {
            throw new UUFException(
                    "An error occurred when pre-processing the Handlebars template '" + templatePath + "'.", e);
        }
        layout = Optional.ofNullable(context.data(DATA_KEY_CURRENT_LAYOUT));
        permission = context.data(DATA_KEY_SECURED);
    }

    public Optional<String> getLayoutName() {
        return layout;
    }

    /**
     * Returns the permission related to the handlebars {{secured}} tag.
     *
     * @return permission related to the handlebars {{secured}} tag.
     */
    public Permission getPermission() {
        return permission;
    }
}
