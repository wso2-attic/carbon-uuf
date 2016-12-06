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

package org.wso2.carbon.uuf.api.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A bean class that represents a node in the dependency tree.
 *
 * @since 1.0.0
 */
public class DependencyNode {

    private String artifactId;
    private String version;
    private String contextPath;
    private List<DependencyNode> dependencies = Collections.emptyList();
    private Set<String> allDependencies = Collections.emptySet();

    /**
     * Returns the artifact ID of the UUF Component which is reflected by this node.
     *
     * @return artifact ID of the UUF Component
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Sets the artifact ID of the UUF Component reflected by this node.
     *
     * @param artifactId artifact ID to be set
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Returns the version of the UUF Component which is reflected by this node.
     *
     * @return version of the UUF Component
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the UUF Component reflected by this node.
     *
     * @param version version to be set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the context path of the UUF Component which is reflected by this node.
     *
     * @return context path of the UUF Component
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the context path of the UUF Component reflected by this node. If the setting context path doesn't start with
     * a '/', then it will be prepended.
     *
     * @param contextPath context path to be set
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath.startsWith("/") ? contextPath : "/" + contextPath;
    }

    /**
     * Returns the dependencies of the UUF Component which is reflected by this node.
     *
     * @return dependencies of the UUF Component
     */
    public List<DependencyNode> getDependencies() {
        return dependencies;
    }

    /**
     * Sets the dependencies list of the UUF Component reflected by this node.
     *
     * @param dependencies dependencies list to be set
     */
    public void setDependencies(List<DependencyNode> dependencies) {
        if (dependencies == null) {
            return;
        }

        this.dependencies = dependencies;
        Set<String> allDependencies = new HashSet<>();
        dependencies.forEach(dependencyNode -> {
            allDependencies.add(dependencyNode.artifactId);
            allDependencies.addAll(dependencyNode.allDependencies);
        });
        this.allDependencies = allDependencies;
    }

    /**
     * Returns all the dependencies (including transitive ones) of the UUF Component which is reflected by this node.
     *
     * @return dependencies of the UUF Component including transitive
     */
    public Set<String> getAllDependencies() {
        return allDependencies;
    }

    /**
     * Traverse this node and its dependencies in depth-first manner.
     *
     * @param nodeConsumer consumer that consumes each node
     */
    public void traverse(Consumer<DependencyNode> nodeConsumer) {
        dependencies.forEach(dependencyNode -> dependencyNode.traverse(nodeConsumer));
        nodeConsumer.accept(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(artifactId, version, contextPath);
    }

    /**
     * {@inheritDoc}
     */
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
