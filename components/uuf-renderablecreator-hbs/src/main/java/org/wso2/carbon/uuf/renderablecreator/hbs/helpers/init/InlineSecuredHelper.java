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

package org.wso2.carbon.uuf.renderablecreator.hbs.helpers.init;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.api.auth.Permission;
import org.wso2.carbon.uuf.renderablecreator.hbs.internal.HbsPreprocessor;

import java.io.IOException;

public class InlineSecuredHelper implements Helper<Object> {

    public static final String HELPER_NAME = "secured";

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        if (!options.tagType.inline()) {
            return "";
        }

        Permission permission = getPermission(context, options);
        options.data(HbsPreprocessor.DATA_KEY_SECURED, permission);
        return "";
    }

    /**
     * Returns the permission from the handle bar context.
     *
     * @param context handlebars context
     * @param options handlebars options
     * @return permission from the handle bar context
     */
    protected Permission getPermission(Object context, Options options) {
        Permission permission;
        if (context instanceof String) {
            // {{secured resourceUri action}}
            String resourceURI = context.toString();
            String action = options.param(0).toString();
            permission = new Permission(resourceURI, action);
        } else {
            // {{secured}}
            permission = Permission.ANY_PERMISSION;
        }
        return permission;
    }
}
