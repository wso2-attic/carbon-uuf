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

package org.wso2.carbon.uuf.core;

public class NameUtils {

    public static String getFullyQualifiedName(String componentName, String name) {
        return isFullyQualifiedName(name) ? name : (componentName + "." + name);
    }

    public static String getSimpleName(String fullyQualifiedName) {
        // <component-name>.<binding/fragment-name>
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        if (lastDot == -1) {
            throw new IllegalArgumentException("Name '" + fullyQualifiedName + "' is not a fully qualified name.");
        }
        return fullyQualifiedName.substring(lastDot + 1);
    }

    public static String getComponentName(String fullyQualifiedName) {
        // <component-name>.<binding/fragment-name>
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        if (lastDot == -1) {
            throw new IllegalArgumentException("Name '" + fullyQualifiedName + "' is not a fully qualified name.");
        }
        return fullyQualifiedName.substring(0, lastDot);
    }

    public static boolean isFullyQualifiedName(String name) {
        return (name.lastIndexOf('.') != -1);
    }
}
