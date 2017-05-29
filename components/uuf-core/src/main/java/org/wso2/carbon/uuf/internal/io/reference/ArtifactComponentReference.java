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

import org.apache.commons.io.FilenameUtils;
import org.wso2.carbon.uuf.api.reference.ComponentReference;
import org.wso2.carbon.uuf.api.reference.FileReference;
import org.wso2.carbon.uuf.api.reference.FragmentReference;
import org.wso2.carbon.uuf.api.reference.LayoutReference;
import org.wso2.carbon.uuf.api.reference.PageReference;
import org.wso2.carbon.uuf.internal.exception.FileOperationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class ArtifactComponentReference implements ComponentReference {

    private static final String CHAR_ENCODING = "UTF-8";
    private final Path componentDirectory;
    private final ArtifactAppReference appReference;

    public ArtifactComponentReference(Path componentDirectory, ArtifactAppReference appReference) {
        this.componentDirectory = componentDirectory;
        this.appReference = appReference;
    }

    @Override
    public Stream<PageReference> getPages(Set<String> supportedExtensions) {
        Path pages = componentDirectory.resolve(DIR_NAME_PAGES);
        if (!Files.exists(pages)) {
            return Stream.<PageReference>empty();
        }
        try {
            return Files.walk(pages)
                    .filter(path -> Files.isRegularFile(path) && supportedExtensions.contains(getExtension(path)))
                    .map(path -> new ArtifactPageReference(path, this));
        } catch (IOException e) {
            throw new FileOperationException("An error occurred while listing pages in '" + pages + "'.", e);
        }
    }

    @Override
    public Stream<LayoutReference> getLayouts(Set<String> supportedExtensions) {
        Path layouts = componentDirectory.resolve(DIR_NAME_LAYOUTS);
        if (!Files.exists(layouts)) {
            return Stream.<LayoutReference>empty();
        }
        try {
            return Files.list(layouts)
                    .filter(path -> Files.isRegularFile(path) && supportedExtensions.contains(getExtension(path)))
                    .map(path -> new ArtifactLayoutReference(path, this));
        } catch (IOException e) {
            throw new FileOperationException("An error occurred while listing layouts in '" + layouts + "'.", e);
        }
    }

    private String getExtension(Path filePath) {
        return FilenameUtils.getExtension(filePath.getFileName().toString());
    }

    @Override
    public Stream<FragmentReference> getFragments(Set<String> supportedExtensions) {
        Path fragments = componentDirectory.resolve(DIR_NAME_FRAGMENTS);
        if (!Files.exists(fragments)) {
            return Stream.<FragmentReference>empty();
        }
        try {
            return Files.list(fragments)
                    .filter(Files::isDirectory)
                    .map(path -> new ArtifactFragmentReference(path, this, supportedExtensions));
        } catch (IOException e) {
            throw new FileOperationException("An error occurred while listing fragments in '" + fragments + "'.", e);
        }
    }

    @Override
    public FileReference getConfiguration() {
        Path configuration = componentDirectory.resolve(FILE_NAME_CONFIGURATION);
        if (Files.exists(configuration)) {
            return new ArtifactFileReference(configuration, appReference);
        } else {
            throw new FileOperationException("Cannot find component's configuration '" + FILE_NAME_CONFIGURATION +
                                                     "' file in component '" + componentDirectory + "'.");
        }
    }

    @Override
    public Optional<FileReference> getOsgiImportsConfig() {
        Path osgiImports = componentDirectory.resolve(FILE_NAME_OSGI_IMPORTS);
        if (Files.exists(osgiImports)) {
            return Optional.of(new ArtifactFileReference(osgiImports, appReference));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Stream<FileReference> getI18nFiles() {
        Path lang = componentDirectory.resolve(DIR_NAME_LANGUAGE);
        if (!Files.exists(lang)) {
            return Stream.<FileReference>empty();
        }
        try {
            return Files.list(lang)
                    .filter(path -> Files.isRegularFile(path) && "properties".equals(getExtension(path)))
                    .map(path -> new ArtifactFileReference(path, appReference));
        } catch (IOException e) {
            throw new FileOperationException("An error occurred while listing language files in '" + lang + "'.", e);
        }
    }

    @Override
    public String getPath() {
        return componentDirectory.toString();
    }

    Path getDirectory() {
        return componentDirectory;
    }

    ArtifactAppReference getAppReference() {
        return appReference;
    }
}
