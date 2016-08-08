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

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.exception.UnauthorizedException;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;

import java.io.IOException;

public class SecuredHelper implements Helper<Object> {

    public static final String HELPER_NAME = "secured";

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        //TODO support dynamic parameters eg:- {{secure “jms://{topicPath}” ”jms:view”}}
        if (context instanceof String) {
            // {{#secured permissionUri permissionAction}} ... {{/secured}} || {{secured permissionUri permissionAction}}
            API api = options.data(HbsRenderable.DATA_KEY_API);
            boolean hasPermission = api.getSession().map(Session::getUser).get().
                    hasPermission(context.toString(),options.param(0));

            if (options.tagType.inline()) {
                if (hasPermission) {
                    return options.fn().toString();
                } else {
                    throw new UnauthorizedException("You don't have permission to access " + context.toString());
                }
            } else {
                return hasPermission ? options.fn().toString() : options.inverse();
            }
        } else {
            // {{#secured}} ... {{/secured}}
            API api = options.data(HbsRenderable.DATA_KEY_API);
            return api.getSession().map(Session::getUser).isPresent() ? options.fn() : options.inverse();
        }
    }
}
