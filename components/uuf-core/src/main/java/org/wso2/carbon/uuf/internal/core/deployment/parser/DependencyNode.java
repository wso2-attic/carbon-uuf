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

package org.wso2.carbon.uuf.internal.core.deployment.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by sajith on 11/30/16.
 */
public class DependencyNode {

    private String artifactId;
    private String version;
    private String contextPath;
    private List<DependencyNode> dependencies;
    private Set<String> allDependencies;

    public DependencyNode() {
        this.dependencies = Collections.emptyList();
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath.startsWith("/") ? contextPath : "/" + contextPath;
    }

    public List<DependencyNode> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<DependencyNode> dependencies) {
        if (dependencies == null) {
            this.dependencies = Collections.emptyList();
            this.allDependencies = Collections.emptySet();
        } else {
            this.dependencies = dependencies;
            Set<String> allDependencies = new HashSet<>();
            dependencies.forEach(dependencyNode -> {
                allDependencies.add(dependencyNode.artifactId);
                allDependencies.addAll(dependencyNode.allDependencies);
            });
            this.allDependencies = allDependencies;
        }
    }

    public Set<String> getAllDependencies() {
        return allDependencies;
    }

    public void traverse(Consumer<DependencyNode> nodeConsumer) {
        dependencies.forEach(dependencyNode -> dependencyNode.traverse(nodeConsumer));
        nodeConsumer.accept(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId, version, contextPath);
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof DependencyNode)) {
            DependencyNode other = (DependencyNode) obj;
            return Objects.equals(this.artifactId, other.artifactId) &&
                    Objects.equals(this.version, other.version) &&
                    Objects.equals(this.contextPath, other.contextPath);
        }
        return false;
    }
}
