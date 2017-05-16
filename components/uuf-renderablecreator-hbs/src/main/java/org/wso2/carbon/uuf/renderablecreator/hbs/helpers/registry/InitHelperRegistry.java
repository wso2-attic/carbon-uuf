/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.uuf.renderablecreator.hbs.helpers.registry;

import com.github.jknack.handlebars.HelperRegistry;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.init.LayoutHelper;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.init.InlineSecuredHelper;

/**
 * Handlebars helpers registry for pre-compilation stage.
 * Secured, Layout, I18n and Missing helper is used in this registry.
 *
 * @since 1.0.0
 */
public class InitHelperRegistry extends RuntimeHelperRegistry {

    /**
     * Registers necessary helpers for pre-compilation stage. We only register
     * init (pre-compile time) helpers from helpers.init package.
     *
     * @param registry the Handlebars registry to be used for helper registration
     */
    protected void registerDefaultHelpers(final HelperRegistry registry) {
        registry.registerHelper(InlineSecuredHelper.HELPER_NAME, new InlineSecuredHelper());
        registry.registerHelper(LayoutHelper.HELPER_NAME, new LayoutHelper());
        registry.registerHelperMissing((context, options) -> "");
    }
}
