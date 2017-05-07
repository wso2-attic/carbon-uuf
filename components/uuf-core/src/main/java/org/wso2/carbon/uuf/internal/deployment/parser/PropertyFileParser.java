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
import org.wso2.carbon.uuf.exception.FileOperationException;
import org.wso2.carbon.uuf.internal.exception.ConfigurationException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * Parser for properties files in an UUF apps, themes, and components.
 *
 * @since 1.0.0
 */
public class PropertyFileParser {

    /**
     * Parses the specified properties file.
     *
     * @param propertiesFile reference to the properties file
     * @return populated properties
     * @throws ConfigurationException if cannot read or parse the specified properties file
     */
    public static Properties parse(FileReference propertiesFile) throws ConfigurationException {
        /* Even though we are not supposed to touch IO packages/classes in this class, unfortunately we have do that
        in here as the API of java.util.Properties class is tightly bounded to java.io package. */

        Properties properties = new Properties();
        try {
            properties.load(new StringReader(propertiesFile.getContent()));
        } catch (FileOperationException e) {
            throw new ConfigurationException(
                    "Cannot read the properties file '" + propertiesFile.getAbsolutePath() + "'.", e);
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException(
                    "Cannot parse the properties file '" + propertiesFile.getAbsolutePath() + "'.", e);
        } catch (IOException e) {
            throw new ConfigurationException(
                    "Cannot load the properties file '" + propertiesFile.getAbsolutePath() + "'.", e);
        }
        return properties;
    }
}
