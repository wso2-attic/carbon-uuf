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

package org.wso2.carbon.uuf.handlebars.helpers.runtime;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.core.API;

import java.io.IOException;

import static org.wso2.carbon.uuf.handlebars.renderable.HbsRenderable.DATA_KEY_API;

public class SecuredHelper implements Helper<Object> {

    public static final String HELPER_NAME = "secured";

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        if (options.tagType.inline()) {
            // Is a {{secured}} inline helper, not the block version.
            return "";
        }

        if (context instanceof String) {
            // {{#secured permissionUri permissionAction}} ... {{/secured}}
            // TODO: 6/6/16 implement this with carbon-security C5
            return null;
        } else {
            // {{#secured}} ... {{/secured}}
            API api = options.data(DATA_KEY_API);
            return api.getSession().map(Session::getUser).isPresent() ? options.fn() : options.inverse();
        }
    }
}
