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

import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.reference.AppReference;
import org.wso2.carbon.uuf.reference.ComponentReference;
import org.wso2.carbon.uuf.reference.ThemeReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class ArtifactAppReference implements AppReference {

    private final Path path;

    public ArtifactAppReference(Path path) {
        this.path = path.normalize().toAbsolutePath();
    }

    @Override
    public String getName() {
        Path fileName = path.getFileName();
        return (fileName == null) ? "" : fileName.toString();
    }

    @Override
    public ComponentReference getComponentReference(String componentSimpleName) {
        Path componentPath = path.resolve(DIR_NAME_COMPONENTS).resolve(componentSimpleName);
        return new ArtifactComponentReference(componentPath, this);
    }

    @Override
    public Stream<ThemeReference> getThemeReferences() {
        Path themes = path.resolve(DIR_NAME_THEMES);
        if (!Files.exists(themes)) {
            return Stream.<ThemeReference>empty();
        }
        try {
            return Files
                    .list(themes)
                    .filter(Files::isDirectory)
                    .map(theme -> new ArtifactThemeReference(theme, this));
        } catch (IOException e) {
            throw new UUFException("An error occurred while listing themes in '" + themes + "'.", e);
        }
    }

    @Override
    public List<String> getDependencies() {
        Path dependencyTreeFile = path.resolve(DIR_NAME_COMPONENTS).resolve(FILE_NAME_DEPENDENCY_TREE);
        try {
            return Files.readAllLines(dependencyTreeFile);
        } catch (IOException e) {
            throw new UUFException(
                    "An error occurred while reading dependencies from file '" + dependencyTreeFile + "'.", e);
        }
    }

    @Override
    public String getPath() {
        return path.toString();
    }

    Path getFilePath() {
        return path;
    }
}
