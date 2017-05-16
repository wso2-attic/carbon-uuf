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

import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.api.auth.Permission;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.init.InlineSecuredHelper;

import java.io.IOException;

/**
 * Implements handlebars {@code {{#secured}}} block helper.
 * <p>
 * Block {@code {{#secured}}} handlebars helper can be used to secure a section of a page or a fragment.
 * <p>
 * The block {@code {{#secured}}} handlebars helper can be used in two ways:
 * <ul>
 * <li>{@code {{#secured}}} Some content {{/secured}}</li>
 * <li>{@code {{#secured "some/resource/uri" "someAction"}}} Some content {{/secured}}</li>
 * </ul>
 * <p>
 * When the {@code {{#secured}}} is used, this means that the permission is for any resource URI and for any action
 * (refer {@link Permission}) and would therefore only check if a user {@link org.wso2.carbon.uuf.api.auth.Session}
 * is available when evaluating the permission.
 * <p>
 * When the {@code {{#secured "some/resource/uri" "someAction"}}} is used, this means that the permission is for
 * resource URI "some/resource/uri" and for action "someAction" and would therefore check if a user
 * {@link org.wso2.carbon.uuf.api.auth.Session} is available, if an {@link org.wso2.carbon.uuf.spi.auth.Authorizer}
 * is configured and if the user has the permission when evaluating the permission.
 *
 * @since 1.0.0
 */
public class BlockSecuredHelper extends InlineSecuredHelper {

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        if (options.tagType.inline()) {
            return "";
        }

        API api = options.data(HbsRenderable.DATA_KEY_API);
        Permission permission = getPermission(context, options);
        return api.hasPermission(permission) ? options.fn() : options.inverse();
    }
}
