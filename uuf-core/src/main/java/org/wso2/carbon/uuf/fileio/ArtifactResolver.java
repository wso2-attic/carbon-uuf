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

import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.uuf.core.create.AppReference;
import org.wso2.carbon.uuf.core.create.AppResolver;
import org.wso2.carbon.uuf.core.exception.PageNotFoundException;
import org.wso2.carbon.uuf.core.exception.UUFException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ArtifactResolver implements AppResolver {

    private final List<Path> paths;

    /**
     * This constructor will assume uufHome as $PRODUCT_HOME/deployment/uufapps
     */
    public ArtifactResolver() {
        this(Utils.getCarbonHome().resolve("deployment").resolve("uufapps"));
    }

    public ArtifactResolver(Path uufHome) {
        paths = getAllApplications(uufHome);
    }

    private List<Path> getAllApplications(Path root) {
        try {
            return Files.list(root).filter(Files::isDirectory).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UUFException(
                    "Error while reading deployment artifacts on '" + root.toString() + "' folder!");
        }
    }

    @Override
    public AppReference resolve(String appName) {
        // app list mush be <white-space> and comma separated. <white-space> in app names not allowed
        for (Path uufAppPath : paths) {
            Path directoryName = uufAppPath.toAbsolutePath().normalize().getFileName();
            if ((directoryName != null) && directoryName.toString().equals(appName)) {
                return new ArtifactAppReference(directoryName);
            }
        }
        throw new PageNotFoundException("Cannot find an app with name '" + appName + "'.");
    }
}
