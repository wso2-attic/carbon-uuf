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

package org.wso2.carbon.uuf.core;

import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.uuf.internal.io.ArtifactAppDeployer;

import java.util.Optional;

public class AppArtifact {

    private final String appName;
    private final Artifact artifact;
    private final ArtifactAppDeployer artifactAppDeployer;

    public AppArtifact(String appName, Artifact artifact, ArtifactAppDeployer artifactAppDeployer) {
        this.appName = appName;
        this.artifact = artifact;
        this.artifactAppDeployer = artifactAppDeployer;
    }

    public String getAppName() {
        return this.appName;
    }

    public Artifact getArtifact() {
        return this.artifact;
    }

    public Optional<App> getApp(String contextPath) {
        return this.artifactAppDeployer.getApp(contextPath);
    }
}
