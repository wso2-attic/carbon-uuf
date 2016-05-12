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

    public static boolean isValid(HttpRequest request) {
        String uri = request.getUri();

        // An URI must begin with '/ & it should have at least two characters.
        if ((uri.length() < 2) || (uri.charAt(0) != '/')) {
            return false;
        }

        // '//' or '..' are not allowed in URIs.
        boolean isPreviousCharInvalid = false;
        for (int i = 0; i < uri.length(); i++) {
            char currentChar = uri.charAt(i);
            if ((currentChar == '/') || (currentChar == '.')) {
                if (isPreviousCharInvalid) {
                    return false;
                } else {
                    isPreviousCharInvalid = true;
                }
            } else {
                isPreviousCharInvalid = false;
            }
        }
        return true;
    }

    public static boolean isStaticResourceRequest(HttpRequest request) {
        return request.getUriWithoutAppContext().startsWith("/public/");
    }

    public static boolean isComponentStaticResourceRequest(HttpRequest request) {
        return request.getUriWithoutAppContext().startsWith(COMPONENT_STATIC_RESOURCES_URI_PREFIX);
    }

    public static boolean isThemeStaticResourceRequest(HttpRequest request) {
        return request.getUriWithoutAppContext().startsWith(THEMES_STATIC_RESOURCES_URI_PREFIX);
    }

    public static boolean isDebugRequest(HttpRequest request) {
        return request.getUriWithoutAppContext().startsWith(DEBUG_APP_URI_PREFIX);
    }

    public static boolean isFragmentRequest(HttpRequest request) {
        return request.getUriWithoutAppContext().startsWith(FRAGMENTS_URI_PREFIX);
    }
}
