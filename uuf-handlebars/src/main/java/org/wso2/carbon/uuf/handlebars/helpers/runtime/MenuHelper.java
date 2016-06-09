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

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.wso2.carbon.uuf.core.Lookup;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.wso2.carbon.uuf.handlebars.renderable.HbsRenderable.DATA_KEY_LOOKUP;

public class MenuHelper implements Helper<Object> {

    public static final String HELPER_NAME = "menu";
    private static final String KEY_THIS = "this";

    @Override
    @SuppressWarnings("unchecked")
    public CharSequence apply(Object context, Options options) throws IOException {
        if (context == null) {
            return "";
        }

        Map<String, Map> menu;
        if (context instanceof String) {
            String menuName = (String) context;
            if (menuName.isEmpty()) {
                throw new IllegalArgumentException("Invalid menu name. Menu name cannot be empty.");
            }
            Lookup lookup = options.data(DATA_KEY_LOOKUP);
            menu = lookup.getConfiguration().getMenu(menuName);
            if (menu == null) {
                return ""; // Menu 'menuName' does not exists.
            }
        } else if (context instanceof Map) {
            Map<?, ?> rawMap = (Map) context;
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (!(entry.getKey() instanceof String)) {
                    throw new IllegalArgumentException("Expected a Map<String, Map> context. Instead found a '" +
                                                               entry.getKey().getClass().getName() + "' key.");
                } else if (!(entry.getValue() instanceof Map)) {
                    throw new IllegalArgumentException("Expected a Map<String, Map> context. Instead found a '" +
                                                               entry.getValue().getClass().getName() + "' value at '" +
                                                               entry.getKey() + "' key.");
                }
            }
            menu = (Map<String, Map>) rawMap;
        } else {
            throw new IllegalArgumentException(
                    "Menu helper context must be either a string (menu name) or a Map<String, Map>. Instead found a '" +
                            context.getClass().getName() + "' context.");
        }
        if (menu.isEmpty()) {
            return "";
        }

        Iterator<Map.Entry<String, Map>> iterator = menu.entrySet().iterator();
        Context parentContext = options.context;
        Map valueOfThisKey = menu.get(KEY_THIS);
        boolean isFirstIteration = true;
        while (iterator.hasNext()) {
            Map.Entry<String, Map> entry = iterator.next();
            if (entry.getKey().equals(KEY_THIS)) {
                continue; // Skip the entry with "@this" key.
            }
            boolean isLeaf = isLeafMenu(entry.getValue());
            Context iterationContext = Context.newContext(parentContext, entry.getValue())
                    .combine("@key", entry.getKey())
                    .combine("@this", valueOfThisKey)
                    .combine("@first", isFirstIteration)
                    .combine("@last", !iterator.hasNext())
                    .combine("@leaf", isLeaf)
                    .combine("@nested", !isLeaf);
            options.buffer().append(options.fn(iterationContext));
            isFirstIteration = false;
        }
        return options.buffer();
    }

    private static boolean isLeafMenu(Map menu) {
        for (Object value : menu.values()) {
            if (!(value instanceof String)) {
                return false;
            }
        }
        return true;
    }
}