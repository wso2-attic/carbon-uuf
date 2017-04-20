/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.internal.io.reference;

import org.wso2.carbon.uuf.api.reference.AppReference;
import org.wso2.carbon.uuf.api.reference.ComponentReference;
import org.wso2.carbon.uuf.api.reference.FileReference;
import org.wso2.carbon.uuf.api.reference.ThemeReference;
import org.wso2.carbon.uuf.exception.FileOperationException;
import org.wso2.carbon.uuf.internal.io.ArtifactFileReference;
import org.wso2.carbon.uuf.internal.io.ArtifactThemeReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ArtifactAppReference implements AppReference {

    private final Path appDirectory;
    private final Path componentsDirectory;
    private final Path customizationsDirectory;

    public ArtifactAppReference(Path appDirectory) {
        this.appDirectory = appDirectory.normalize().toAbsolutePath();
        this.componentsDirectory = this.appDirectory.resolve(DIR_NAME_COMPONENTS);
        this.customizationsDirectory = this.appDirectory.resolve(DIR_NAME_CUSTOMIZATIONS);
    }

    @Override
    public String getName() {
        Path fileName = appDirectory.getFileName();
        return (fileName == null) ? "" : fileName.toString();
    }

    @Override
    public ComponentReference getComponentReference(String componentContext) {
        String componentDirName = componentContext.startsWith("/") ? componentContext.substring(1) : componentContext;

        // check the customizations directory first to load the customized version of the component.
        Path customizedComponentDirectory = customizationsDirectory.resolve(componentDirName);
        if (Files.exists(customizedComponentDirectory)) {
            return new ArtifactComponentReference(customizedComponentDirectory, this);
        }
        // Didn't found the customized version, so try to find it in the components directory.
        Path componentDirectory = componentsDirectory.resolve(componentDirName);
        if (Files.exists(componentDirectory)) {
            return new ArtifactComponentReference(componentDirectory, this);
        } else {
            throw new FileOperationException("Cannot find the component for '" + componentContext +
                                                     "' context path in app '" + appDirectory + "'.");
        }
    }

    @Override
    public Stream<ThemeReference> getThemeReferences() {
        Path themesDirectory = this.appDirectory.resolve(DIR_NAME_THEMES);
        if (!Files.exists(themesDirectory)) {
            return Stream.<ThemeReference>empty();
        }
        try {
            return Files.list(themesDirectory)
                    .filter(Files::isDirectory)
                    .map(path -> new ArtifactThemeReference(path, this));
        } catch (IOException e) {
            throw new FileOperationException("An error occurred while listing themes in '" + themesDirectory + "'.", e);
        }
    }

    @Override
    public FileReference getConfiguration() {
        Path configuration = componentsDirectory.resolve(FILE_NAME_CONFIGURATION);
        if (Files.exists(configuration)) {
            return new ArtifactFileReference(configuration, this);
        } else {
            throw new FileOperationException("Cannot find app's configuration file '" + FILE_NAME_CONFIGURATION +
                                                     "' in app '" + appDirectory + "'.");
        }
    }

    @Override
    public FileReference getDependencyTree() {
        Path dependencyTree = componentsDirectory.resolve(FILE_NAME_DEPENDENCY_TREE);
        if (Files.exists(dependencyTree)) {
            return new ArtifactFileReference(dependencyTree, this);
        } else {
            throw new FileOperationException("Cannot find dependency tree file '" + FILE_NAME_DEPENDENCY_TREE +
                                                     "' in app '" + appDirectory + "'.");
        }
    }

    @Override
    public String getPath() {
        return appDirectory.toString();
    }

    Path getDirectory() {
        return appDirectory;
    }
}
