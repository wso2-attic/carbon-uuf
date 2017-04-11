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

package org.wso2.carbon.uuf.api.config;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Locale;
import java.util.Properties;

/**
 * Test cases for I18nResources.
 *
 * @since 1.0.0
 */
public class I18nResourcesTest {

    private static final String MESSAGE_KEY_HELLO = "test.hello";
    private static final String MESSAGE_KEY_HELLO_NAME = "test.hello.name";

    private static I18nResources createI18nResources() {
        I18nResources i18nResources = new I18nResources();

        Properties frenchMessages = new Properties();
        frenchMessages.put(MESSAGE_KEY_HELLO, "Bonjour");
        frenchMessages.put(MESSAGE_KEY_HELLO_NAME, "Bonjour {0}");
        i18nResources.addI18nResource(Locale.FRENCH, frenchMessages);

        Properties sinhalaMessages = new Properties();
        sinhalaMessages.put(MESSAGE_KEY_HELLO, "හෙලෝ");
        sinhalaMessages.put(MESSAGE_KEY_HELLO_NAME, "හෙලෝ {0}");
        i18nResources.addI18nResource(Locale.forLanguageTag("si"), sinhalaMessages);

        Properties englishMessages = new Properties();
        englishMessages.put(MESSAGE_KEY_HELLO, "Hello");
        englishMessages.put(MESSAGE_KEY_HELLO_NAME, "Hello {0}");
        i18nResources.addI18nResource(Locale.ENGLISH, englishMessages);

        return i18nResources;
    }

    @DataProvider
    public Object[][] locales() {
        return new Object[][]{
                {null, null},
                {"", null},
                {"&,,-#_", null},
                {"en", Locale.ENGLISH},
                {"en-GB, en-US;q=.8, en-CA;q=0.6, en;q=0.1", Locale.ENGLISH},
                {"zh,zh-CN,zh-tw,ko,ko-kr", null},
                {"fr-FR,fr", Locale.FRENCH},
                {"si", Locale.forLanguageTag("si")},
                {"ja", null}
        };
    }

    @Test(dataProvider = "locales")
    public void testGetLocale(String localeString, Locale expectedLocale) {
        I18nResources i18nResources = createI18nResources();
        Assert.assertEquals(i18nResources.getLocale(localeString), expectedLocale);
    }
}
