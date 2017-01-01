/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.uuf.internal.deployment.parser.bean.AppConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Parser for app's configuration YAML file in an UUF App.
 *
 * @since 1.0.0
 */
public class AppConfigParser {

    /**
     * Parses the specified app's configuration YAML file.
     *
     * @param configFile reference to app's configuration YAML file
     * @return app's configuration
     * @throws MalformedConfigurationException if cannot parse the specified configuration file
     */
    public static AppConfig parse(FileReference configFile) {
        try {
            return getYamlParser().loadAs(configFile.getContent(), AppConfig.class);
        } catch (Exception e) {
            throw new MalformedConfigurationException(
                    "Cannot parse app's configuration file '" + configFile.getAbsolutePath() + "'.", e);
        }
    }

    static Yaml getYamlParser() {
        return new Yaml(new CustomClassLoaderConstructor());
    }

    private static class CustomClassLoaderConstructor extends Constructor {

        private final ClassLoader classLoader;

        public CustomClassLoaderConstructor() {
            super(Object.class);
            this.classLoader = CustomClassLoaderConstructor.class.getClassLoader();
        }

        @Override
        protected Class<?> getClassForName(String name) throws ClassNotFoundException {
            return Class.forName(name, true, classLoader);
        }
    }
}
