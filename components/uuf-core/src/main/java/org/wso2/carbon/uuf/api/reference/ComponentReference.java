/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.uuf.api.reference;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

public interface ComponentReference {

    String DIR_NAME_PAGES = "pages";
    String DIR_NAME_LAYOUTS = "layouts";
    String DIR_NAME_FRAGMENTS = "fragments";
    String DIR_NAME_LANGUAGE = "lang";
    String FILE_NAME_MANIFEST = "component.yaml";
    String FILE_NAME_CONFIGURATIONS = "config.yaml";
    String FILE_NAME_OSGI_IMPORTS = "osgi-imports";

    Stream<PageReference> getPages(Set<String> supportedExtensions);

    Stream<LayoutReference> getLayouts(Set<String> supportedExtensions);

    Stream<FragmentReference> getFragments(Set<String> supportedExtensions);

    Optional<FileReference> getManifest();

    Optional<FileReference> getConfiguration();

    Optional<FileReference> getOsgiImportsConfig();

    Map<String, Properties> getI18nFiles();

    String getPath();
}
