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

package org.wso2.carbon.uuf.internal.util;

import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Layout;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Theme;
import org.wso2.carbon.uuf.internal.io.StaticResolver;

public class UriUtils {

    public static final String COMPONENT_STATIC_RESOURCES_URI_PREFIX = "/public/components";
    public static final String THEMES_STATIC_RESOURCES_URI_PREFIX = "/public/themes/";
    public static final String DEBUG_APP_URI_PREFIX = "/debug/";
    public static final String FRAGMENTS_URI_PREFIX = "/fragments/";

    public static String getAppContext(String uri) {
        int secondSlash = uri.indexOf('/', 1); // An URI must start with a slash.
        if (secondSlash == -1) {
            // There is only one slash in the URI.
            return null;
        } else {
            return uri.substring(0, secondSlash);
        }
    }

    public static String getUriWithoutAppContext(String uri) {
        int secondSlash = uri.indexOf('/', 1); // An URI must start with a slash.
        if (secondSlash == -1) {
            // There is only one slash in the URI.
            return null;
        } else {
            return uri.substring(secondSlash, uri.length());
        }
    }

    public static String getPublicUri(Component component, Page page) {
        return COMPONENT_STATIC_RESOURCES_URI_PREFIX + component.getContext() + "/" +
                StaticResolver.DIR_NAME_COMPONENT_RESOURCES;
    }

    public static String getPublicUri(Component component, Layout layout) {
        return COMPONENT_STATIC_RESOURCES_URI_PREFIX + component.getContext() + "/" +
                StaticResolver.DIR_NAME_COMPONENT_RESOURCES;
    }

    public static String getPublicUri(Component component, Fragment fragment) {
        return COMPONENT_STATIC_RESOURCES_URI_PREFIX + component.getContext() + "/" + fragment.getSimpleName();
    }

    public static String getPublicUri(Theme theme) {
        return THEMES_STATIC_RESOURCES_URI_PREFIX + theme.getName();
    }
}
