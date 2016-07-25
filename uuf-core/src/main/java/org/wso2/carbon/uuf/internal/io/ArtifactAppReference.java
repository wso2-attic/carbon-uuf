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

package org.wso2.carbon.uuf.internal.io;

import org.wso2.carbon.uuf.exception.FileOperationException;
import org.wso2.carbon.uuf.reference.AppReference;
import org.wso2.carbon.uuf.reference.ComponentReference;
import org.wso2.carbon.uuf.reference.ThemeReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
    public ComponentReference getComponentReference(String componentSimpleName) {
        Path componentDirectory = customizationsDirectory.resolve(componentSimpleName);
        if (!Files.exists(componentDirectory)) {
            componentDirectory = componentsDirectory.resolve(componentSimpleName);
        }
        return new ArtifactComponentReference(componentDirectory, this);
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
    public List<String> getDependencies() {
        Path dependencyTreeFile = componentsDirectory.resolve(FILE_NAME_DEPENDENCY_TREE);
        try {
            return Files.readAllLines(dependencyTreeFile);
        } catch (IOException e) {
            throw new FileOperationException(
                    "An error occurred while reading dependencies from file '" + dependencyTreeFile + "'.", e);
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
