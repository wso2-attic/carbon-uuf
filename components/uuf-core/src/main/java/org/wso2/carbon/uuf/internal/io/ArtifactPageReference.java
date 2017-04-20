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
import org.wso2.carbon.uuf.api.reference.ComponentReference;
import org.wso2.carbon.uuf.api.reference.FileReference;
import org.wso2.carbon.uuf.api.reference.PageReference;
import org.wso2.carbon.uuf.internal.io.reference.ArtifactComponentReference;

import java.nio.file.Path;

import static org.wso2.carbon.uuf.api.reference.ComponentReference.DIR_NAME_PAGES;

public class ArtifactPageReference implements PageReference {

    private final Path pageFile;
    private final ArtifactComponentReference componentReference;

    public ArtifactPageReference(Path pageFile, ArtifactComponentReference componentReference) {
        this.pageFile = pageFile;
        this.componentReference = componentReference;
    }

    @Override
    public String getPathPattern() {
        StringBuilder sb = new StringBuilder();
        Path pagesDirectory = componentReference.getDirectory().resolve(DIR_NAME_PAGES).relativize(pageFile);
        for (Path path : pagesDirectory) {
            sb.append('/').append(FilenameUtils.removeExtension(path.toString()));
        }
        return sb.toString();
    }

    @Override
    public FileReference getRenderingFile() {
        return new ArtifactFileReference(pageFile, componentReference.getAppReference());
    }

    @Override
    public ComponentReference getComponentReference() {
        return componentReference;
    }
}
