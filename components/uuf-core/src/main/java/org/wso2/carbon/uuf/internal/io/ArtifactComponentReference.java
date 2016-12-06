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

package org.wso2.carbon.uuf.internal.io;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.wso2.carbon.uuf.api.reference.ComponentReference;
import org.wso2.carbon.uuf.api.reference.FileReference;
import org.wso2.carbon.uuf.api.reference.FragmentReference;
import org.wso2.carbon.uuf.api.reference.LayoutReference;
import org.wso2.carbon.uuf.api.reference.PageReference;
import org.wso2.carbon.uuf.exception.FileOperationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
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
    public Optional<FileReference> getManifest() {
        Path componentManifest = componentDirectory.resolve(FILE_NAME_MANIFEST);
        if (Files.exists(componentManifest)) {
            return Optional.of(new ArtifactFileReference(componentManifest, appReference));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FileReference> getConfiguration() {
        Path configuration = componentDirectory.resolve(FILE_NAME_CONFIGURATIONS);
        if (Files.exists(configuration)) {
            return Optional.of(new ArtifactFileReference(configuration, appReference));
        } else {
            return Optional.empty();
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
    public Map<String, Properties> getI18nFiles() {
        Path lang = componentDirectory.resolve(DIR_NAME_LANGUAGE);
        Map<String, Properties> i18n = new HashMap<>();
        DirectoryStream<Path> stream = null;
        if (!Files.exists(lang)) {
            return i18n;
        }

        try {
            stream = Files.newDirectoryStream(lang, "*.{properties}");
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    Properties props = new Properties();
                    InputStreamReader is = null;
                    String file = entry.toString();
                    try {
                        is = new InputStreamReader(new FileInputStream(file), CHAR_ENCODING);
                        props.load(is);
                    } finally {
                        IOUtils.closeQuietly(is);
                    }

                    Path path = entry.getFileName();
                    if (path != null) {
                        String fileName = path.toString();
                        i18n.put(fileName.substring(0, fileName.indexOf('.')), props);
                    }
                }
            }
        } catch (IOException e) {
            throw new FileOperationException("An error occurred while reading locale files in '" + lang + "'.", e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return i18n;
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
