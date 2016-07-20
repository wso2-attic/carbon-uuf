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
import org.wso2.carbon.uuf.reference.ComponentReference;
import org.wso2.carbon.uuf.reference.FileReference;
import org.wso2.carbon.uuf.reference.FragmentReference;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class ArtifactFragmentReference implements FragmentReference {

    private final Path path;
    private final ArtifactComponentReference componentReference;
    private final Set<String> supportedExtensions;

    public ArtifactFragmentReference(Path path, ArtifactComponentReference componentReference,
                                     Set<String> supportedExtensions) {
        this.path = path;
        this.componentReference = componentReference;
        this.supportedExtensions = supportedExtensions;
    }

    @Override
    public String getName() {
        Path fileName = path.getFileName(); // Name of the fragment is the name of the directory.
        return (fileName == null) ? "" : fileName.toString();
    }

    @Override
    public FileReference getRenderingFile() {
        String fragmentName = getName();
        for (String extension : supportedExtensions) {
            Path renderingFilePath = path.resolve(fragmentName + "." + extension);
            if (Files.isRegularFile(renderingFilePath)) {
                return new ArtifactFileReference(renderingFilePath, componentReference.getAppReference());
            }
        }
        throw new UUFException("Fragment '" + fragmentName + "' of component '" + componentReference.getPath() +
                                       "' is empty.");
    }

    @Override
    public ComponentReference getComponentReference() {
        return componentReference;
    }
}
