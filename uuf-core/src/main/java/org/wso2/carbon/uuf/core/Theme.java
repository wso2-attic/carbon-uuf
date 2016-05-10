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

import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.carbon.uuf.fileio.StaticResolver.DIR_NAME_COMPONENT_RESOURCES;
import static org.wso2.carbon.uuf.fileio.RequestUtil.THEMES_STATIC_RESOURCES_URI_PREFIX;

public class Theme {

    private final String name;
    private final List<String> cssTagSuffixes;
    private final List<String> headJsTagSuffixes;
    private final List<String> bodyJsTagSuffixes;

    public Theme(String name, List<String> cssRelativePaths, List<String> headJsRelativePaths,
                 List<String> bodyJsRelativePaths) {
        this.name = name;

        String uriPrefix = THEMES_STATIC_RESOURCES_URI_PREFIX + name + "/" + DIR_NAME_COMPONENT_RESOURCES + "/";
        this.cssTagSuffixes = cssRelativePaths.stream()
                .map(relativePath -> uriPrefix + relativePath + "\" rel=\"stylesheet\" type=\"text/css\" />")
                .collect(Collectors.toList());
        this.headJsTagSuffixes = headJsRelativePaths.stream()
                .map(relativePath -> uriPrefix + relativePath + "\" type=\"text/javascript\"></script>")
                .collect(Collectors.toList());
        this.bodyJsTagSuffixes = bodyJsRelativePaths.stream()
                .map(relativePath -> uriPrefix + relativePath + "\" type=\"text/javascript\"></script>")
                .collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public void render(RequestLookup requestLookup) {
        String appContext = requestLookup.getAppContext();
        for (String cssTagSuffix : cssTagSuffixes) {
            requestLookup.addToPlaceholder(Placeholder.CSS, "<link href=\"" + appContext + cssTagSuffix);
        }
        for (String headJsTagSuffix : headJsTagSuffixes) {
            requestLookup.addToPlaceholder(Placeholder.HEAD_JS, "<script src=\"" + appContext + headJsTagSuffix);
        }
        for (String bodyJsTagSuffix : bodyJsTagSuffixes) {
            requestLookup.addToPlaceholder(Placeholder.BODY_JS, "<script src=\"" + appContext + bodyJsTagSuffix);
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Theme) && (this.name.equals(((Theme) obj).name));
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "}";
    }
}
