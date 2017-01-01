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

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MenuHelper implements Helper<Object> {

    public static final String HELPER_NAME = "menu";

    @Override
    @SuppressWarnings("unchecked")
    public CharSequence apply(Object context, Options options) throws IOException {
        if (Handlebars.Utils.isEmpty(context)) {
            return "";
        }

        List<Configuration.MenuItem> menuItems;
        if (context instanceof String) {
            String menuName = (String) context;
            Lookup lookup = options.data(HbsRenderable.DATA_KEY_LOOKUP);
            menuItems = lookup.getConfiguration().getMenu(menuName);
        } else if (context instanceof Configuration.MenuItem[]) {
            menuItems = Arrays.asList((Configuration.MenuItem[]) context);
        } else if (context instanceof List) {
            List<?> rawList = (List) context;
            for (Object item : rawList) {
                if (!(item instanceof Configuration.MenuItem)) {
                    throw new IllegalArgumentException(
                            "List parsed as the context of the menu helper must be a List<Configuration.MenuItem>. " +
                                    "Instead found a '" + context.getClass().getName() + "'.");
                }
            }
            menuItems = (List<Configuration.MenuItem>) rawList;
        } else {
            throw new IllegalArgumentException(
                    "Menu helper context must be either a string (menu name) or a Configuration.MenuItem[] or a " +
                            "List<Configuration.MenuItem>. Instead found a '" + context.getClass().getName() + "'.");
        }

        Iterator<Configuration.MenuItem> iterator = menuItems.iterator();
        Context parentContext = options.context;
        boolean isFirstIteration = true;
        while (iterator.hasNext()) {
            Configuration.MenuItem menuItem = iterator.next();
            boolean isLeaf = menuItem.getSubmenus().isEmpty();
            Context iterationContext = Context.newContext(parentContext, menuItem)
                    .combine("@first", isFirstIteration)
                    .combine("@last", !iterator.hasNext())
                    .combine("@leaf", isLeaf)
                    .combine("@nested", !isLeaf);
            options.buffer().append(options.fn(iterationContext));
            isFirstIteration = false;
        }
        return options.buffer();
    }
}
