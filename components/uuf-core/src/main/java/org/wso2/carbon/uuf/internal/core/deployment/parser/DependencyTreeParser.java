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

import org.wso2.carbon.uuf.api.config.DependencyNode;
import org.wso2.carbon.uuf.api.reference.FileReference;
import org.wso2.carbon.uuf.exception.MalformedConfigurationException;
import org.yaml.snakeyaml.Yaml;

/**
 * Parser for dependency tree file in an UUF App.
 *
 * @since 1.0.0
 */
public class DependencyTreeParser {

    /**
     * Parses the specified dependency tree YAML file.
     *
     * @param dependencyTreeFile reference to the dependency tree YAML file
     * @return root node of the dependecy tree
     * @exception MalformedConfigurationException if cannot parse specified dependency tree file
     */
    public static DependencyNode parse(FileReference dependencyTreeFile) {
        try {
            return new Yaml().loadAs(dependencyTreeFile.getContent(), DependencyNode.class);
        } catch (Exception e) {
            throw new MalformedConfigurationException(
                    "Cannot parse dependency tree file '" + dependencyTreeFile.getAbsolutePath() + "'.", e);
        }
    }
}
