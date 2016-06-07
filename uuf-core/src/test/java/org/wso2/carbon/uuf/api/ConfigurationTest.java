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

package org.wso2.carbon.uuf.api;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationTest {
    @Test
    public void testMergeConfiguration() {
        //menu item home
        Map<String, Object> homeMenuItem = createMenuItem("fw fw-home", "#home");
        //sub menu pets
        Map<String, Object> seePetsMenuItem = createMenuItem("fw fw-list", "pets");
        Map<String, Object> addNewPetItem = createMenuItem("fw fw-add", "pets/new");
        Map<String, Object> petsSubMenu = new HashMap<>();
        petsSubMenu.put("See all pets", seePetsMenuItem);
        petsSubMenu.put("Add a new pet", addNewPetItem);
        //main menu
        Map<String, Object> mainMenu = new HashMap<>();
        mainMenu.put("Home", homeMenuItem);
        mainMenu.put("Pets", petsSubMenu);
        Map<String, Object> menu = new HashMap<>();
        menu.put("main", mainMenu);
        //menu
        Map<String, Object> config = new HashMap<>();
        config.put("menu", menu);

        Configuration originalConfig = new Configuration(config);
        //----------------Other Configuration------------------//
        //menu item home
        Map<String, Object> homeMenuItemOther = createMenuItem("fw fw-home", "#");
        //sub menu devices
        Map<String, Object> seeDevicesMenuItem = createMenuItem("fw fw-list", "devices");
        Map<String, Object> addNewDeviceItem = createMenuItem("fw fw-add", "devices/new");
        Map<String, Object> devicesSubMenu = new HashMap<>();
        devicesSubMenu.put("See all devices", seeDevicesMenuItem);
        devicesSubMenu.put("Add a new device", addNewDeviceItem);
        Map<String, Object> deletePetsMenuItem = createMenuItem("fw fw-delete", "pets/remove");
        Map<String, Object> petsSubMenuOther = new HashMap<>();
        petsSubMenuOther.put("Delete a pet", deletePetsMenuItem);
        //main menu - other
        Map<String, Object> mainMenuOther = new HashMap<>();
        mainMenuOther.put("Home", homeMenuItemOther);
        mainMenuOther.put("Devices", devicesSubMenu);
        mainMenuOther.put("Pets", petsSubMenuOther);
        Map<String, Object> menuOther = new HashMap<>();
        menuOther.put("main", mainMenuOther);
        //menu other
        Map<String, Object> configOther = new HashMap<>();
        configOther.put("menu", menuOther);

        originalConfig.merge(configOther);
        //check whether Devices added on mainMenu
        System.out.println(mainMenu);
        Assert.assertEquals(mainMenu.size(), 3);
        Assert.assertEquals(((Map) mainMenu.get("Devices")).size(), 2);
    }

    private Map<String, Object> createMenuItem(String icon, String link) {
        Map<String, Object> menuItem = new HashMap<>();
        menuItem.put("icon", icon);
        menuItem.put("link", link);
        return menuItem;
    }
}
