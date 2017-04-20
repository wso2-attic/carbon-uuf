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

import java.util.stream.Stream;

/**
 * A reference to an UUF app artifact.
 *
 * @since 1.0.0
 */
public interface AppReference {

    /**
     * Name of the directory that contains UUF components.
     */
    String DIR_NAME_COMPONENTS = "components";
    /**
     * Name of the directory that contains customized UUF components.
     */
    String DIR_NAME_CUSTOMIZATIONS = "customizations";
    /**
     * Name of the directory that contains UUF themes.
     */
    String DIR_NAME_THEMES = "themes";
    /**
     * Name of the file that has the dependency tree of the UUF components.
     */
    String FILE_NAME_DEPENDENCY_TREE = "dependency-tree.yaml";
    /**
     * Name of the file that has the app configurations.
     */
    String FILE_NAME_CONFIGURATION = "configuration.yaml";

    /**
     * Returns the name of the UUF app represented by this reference.
     *
     * @return name of the UUF app
     */
    String getName();

    /**
     * Returns a reference to the UUF component that belongs to the UUF app represented by this reference.
     *
     * @param componentContext context of the UUF component
     * @return reference to the UUF component
     */
    ComponentReference getComponentReference(String componentContext);

    /**
     * Returns UUF theme reference of the UUF app represented by this reference.
     *
     * @return references for UUF themes of this UUF app
     */
    Stream<ThemeReference> getThemeReferences();

    /**
     * Returns a reference to the dependency tree file of the UUF app represented by this reference.
     *
     * @return reference to the dependency tree file
     * @see #FILE_NAME_DEPENDENCY_TREE
     */
    FileReference getDependencyTree();

    /**
     * Returns a reference to the configuration file of the UUF app represented by this reference.
     *
     * @return reference to the configuration file of this UUF app
     * @see #FILE_NAME_CONFIGURATION
     */
    FileReference getConfiguration();

    /**
     * Returns the absolute path to the UUF app represented by this reference.
     *
     * @return absolute path to the UUF app
     */
    String getPath();
}
