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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.reference.FileReference;
import org.wso2.carbon.uuf.exception.FileOperationException;
import org.wso2.carbon.uuf.internal.exception.ConfigurationException;

import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for properties file parser.
 *
 * @since 1.0.0
 */
public class PropertyFileParserTest {

    @Test
    public void testCannotReadFile() {
        FileReference propertiesFile = mock(FileReference.class);
        when(propertiesFile.getContent()).thenThrow(FileOperationException.class);

        Assert.assertThrows(ConfigurationException.class, () -> PropertyFileParser.parse(propertiesFile));
    }

    @Test
    public void testInvalidFile() {
        FileReference propertiesFile = mock(FileReference.class);
        when(propertiesFile.getContent()).thenReturn("# testing\n" +
                                                             "pets-store.all=See all our amazing pets\\u\n");

        Assert.assertThrows(ConfigurationException.class, () -> PropertyFileParser.parse(propertiesFile));
    }

    @Test
    public void test() {
        FileReference propertiesFile = mock(FileReference.class);
        when(propertiesFile.getContent()).thenReturn("# testing\n" +
                                                             "pets-store.all=See all our amazing pets\n" +
                                                             "pets-store.add=Ajouter un nouvel animal de compagnie\n" +
                                                             "pets-store.petOfTheMonth=මාසයේ සුරතලා\n");

        Properties properties = PropertyFileParser.parse(propertiesFile);
        Assert.assertEquals(properties.size(), 3);
        Assert.assertEquals(properties.get("pets-store.all"), "See all our amazing pets");
        Assert.assertEquals(properties.get("pets-store.add"), "Ajouter un nouvel animal de compagnie");
        Assert.assertEquals(properties.get("pets-store.petOfTheMonth"), "මාසයේ සුරතලා");
    }
}
