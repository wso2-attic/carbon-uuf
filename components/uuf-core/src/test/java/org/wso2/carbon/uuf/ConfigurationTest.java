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

package org.wso2.carbon.uuf;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class ConfigurationTest {

    public static Map<?, ?> loadConfiguration(String filename) throws Exception {
        String content = IOUtils.toString(ConfigurationTest.class.getResourceAsStream("/" + filename), "UTF-8");
        return new Yaml().loadAs(content, Map.class);
    }

    @Test
    public void testMergeConfiguration() throws Exception {
        Configuration configuration = new Configuration(loadConfiguration("config-1.yaml"));
        configuration.merge(loadConfiguration("config-2.yaml"));

        Map<String, Map> mainMenu = configuration.getMenu("main");
        Assert.assertEquals(mainMenu.size(), 3);
        Assert.assertTrue(mainMenu.get("Home") != null);
        Assert.assertTrue(mainMenu.get("Home").get("link").equals("#home-2"));
        Assert.assertTrue(mainMenu.get("Pets") != null);
        Assert.assertEquals(mainMenu.get("Pets").size(), 3);
        Assert.assertTrue(mainMenu.get("Pets").get("See all pets") instanceof Map);
        Assert.assertEquals(((Map) mainMenu.get("Pets").get("See all pets")).get("link"), "/pets");
        Assert.assertTrue(mainMenu.get("Devices") != null);
        Assert.assertEquals(mainMenu.get("Devices").size(), 2);
        Assert.assertTrue(mainMenu.get("Devices").get("See all devices") instanceof Map);
        Assert.assertEquals(((Map) mainMenu.get("Devices").get("See all devices")).get("link"), "/devices");

        Map<String, Map> sideMenu = configuration.getMenu("side");
        Assert.assertEquals(sideMenu.size(), 1);

        Assert.assertEquals(configuration.get("appName"), "test app 2");
    }
}
