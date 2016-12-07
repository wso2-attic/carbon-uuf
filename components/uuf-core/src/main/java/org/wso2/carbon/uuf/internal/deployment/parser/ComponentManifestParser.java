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

package org.wso2.carbon.uuf.internal.deployment.parser;

import org.wso2.carbon.uuf.api.config.ComponentManifest;
import org.wso2.carbon.uuf.api.reference.FileReference;
import org.wso2.carbon.uuf.exception.MalformedConfigurationException;
import org.yaml.snakeyaml.Yaml;

/**
 * Parser for component manifest YAML file in an UUF Component.
 *
 * @since 1.0.0
 */
public class ComponentManifestParser {

    /**
     * Parses the specified component manifest YAML file.
     *
     * @param componentManifestFile path to component manifest YAML file
     * @return component manifest
     * @exception MalformedConfigurationException if cannot parse the specified component manifest file
     */
    public static ComponentManifest parse(FileReference componentManifestFile) {
        try {
            return new Yaml().loadAs(componentManifestFile.getContent(), ComponentManifest.class);
        } catch (Exception e) {
            throw new MalformedConfigurationException(
                    "Cannot parse component manifest file '" + componentManifestFile.getAbsolutePath() + "'.", e);
        }
    }
}
