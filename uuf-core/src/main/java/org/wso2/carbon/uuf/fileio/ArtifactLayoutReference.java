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

package org.wso2.carbon.uuf.fileio;

import org.apache.commons.io.FilenameUtils;
import org.wso2.carbon.uuf.core.create.FileReference;
import org.wso2.carbon.uuf.core.create.LayoutReference;

import java.nio.file.Path;

public class ArtifactLayoutReference implements LayoutReference {

    private final Path path;
    private final ArtifactComponentReference componentReference;

    public ArtifactLayoutReference(Path path, ArtifactComponentReference componentReference) {
        this.path = path;
        this.componentReference = componentReference;
    }

    @Override
    public String getName() {
        Path fileName = path.getFileName();
        return (fileName == null) ? "" : FilenameUtils.removeExtension(fileName.toString());
    }

    @Override
    public FileReference getRenderingFile() {
        return new ArtifactFileReference(path, componentReference);
    }
}
