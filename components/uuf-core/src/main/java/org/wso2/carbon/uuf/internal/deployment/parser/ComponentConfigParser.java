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

import org.wso2.carbon.uuf.api.reference.FileReference;
import org.wso2.carbon.uuf.exception.MalformedConfigurationException;
import org.wso2.carbon.uuf.internal.deployment.parser.bean.ComponentConfig;

/**
 * Parser for component's configuration YAML file in an UUF Component.
 *
 * @since 1.0.0
 */
public class ComponentConfigParser {

    /**
     * Parses the specified component's configuration YAML file.
     *
     * @param configFile reference to component's configuration YAML file
     * @return component's configuration
     * @throws MalformedConfigurationException if cannot parse the specified configuration file
     */
    public static ComponentConfig parse(FileReference configFile) {
        ComponentConfig componentConfig;
        try {
            componentConfig = AppConfigParser.getYamlParser().loadAs(configFile.getContent(), ComponentConfig.class);
        } catch (Exception e) {
            throw new MalformedConfigurationException(
                    "Cannot parse component's configuration file '" + configFile.getAbsolutePath() + "'.", e);
        }

        // Parsed component config can be null if the configuration file is empty or has comments only.
        return (componentConfig == null) ? new ComponentConfig() : componentConfig;
    }
}
