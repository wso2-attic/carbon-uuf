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

import org.wso2.carbon.uuf.api.HttpRequest;

public class RequestUtil {

    public static final String COMPONENT_STATIC_RESOURCES_URI_PREFIX = "/public/components";
    public static final String THEMES_STATIC_RESOURCES_URI_PREFIX = "/public/themes";
    public static final String DEBUG_APP_URI_PREFIX = "/debug/";

    public static boolean isStaticResourceUri(HttpRequest request) {
        return request.getUriComponents().getUriWithoutAppContext().startsWith(COMPONENT_STATIC_RESOURCES_URI_PREFIX);
    }

    public static boolean isDebugUri(HttpRequest request) {
        return request.getUriComponents().getUriWithoutAppContext().startsWith(DEBUG_APP_URI_PREFIX);
    }

    public static String getAppName(HttpRequest request) {
        return request.getUriComponents().getAppName();
    }

    public static String getAppContext(HttpRequest request) {
        return request.getUriComponents().getAppContext();
    }
}
