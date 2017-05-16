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

package org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.api.config.I18nResources;
import org.wso2.carbon.uuf.api.model.MapModel;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.HbsPageRenderable;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.model.Model;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Test cases for the i18n helper.
 *
 * @since 1.0.0
 */
public class I18nHelperTest {

    private static final String MESSAGE_KEY_HELLO = "test.hello";
    private static final String MESSAGE_KEY_HELLO_NAME = "test.hello.name";

    private static HbsRenderable createRenderable(String sourceStr) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsPageRenderable(stringTemplateSource);
    }

    private static Lookup createLookup() {
        Lookup lookup = mock(Lookup.class);
        Configuration configuration = createConfiguration();
        when(lookup.getConfiguration()).thenReturn(configuration);
        I18nResources i18nResources = createI18nResources();
        when(lookup.getI18nResources()).thenReturn(i18nResources);
        return lookup;
    }

    private static I18nResources createI18nResources() {
        I18nResources i18nResources = new I18nResources();

        Properties japaneseMessages = new Properties();
        japaneseMessages.put(MESSAGE_KEY_HELLO, "こんにちは");
        japaneseMessages.put(MESSAGE_KEY_HELLO_NAME, "こんにちは{0}");
        i18nResources.addI18nResource(Locale.JAPANESE, japaneseMessages);

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

    private static Configuration createConfiguration() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.other()).thenReturn(Collections.emptyMap());
        return configuration;
    }

    private static RequestLookup createRequestLookup() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getQueryParams()).thenReturn(Collections.emptyMap());
        return spy(new RequestLookup("/contextPath", request, null));
    }

    private static API createAPI() {
        API api = mock(API.class);
        when(api.getSession()).thenReturn(Optional.empty());
        return api;
    }

    @Test
    public void testLocaleFromParam() {
        HbsRenderable renderable = createRenderable("{{i18n \"" + MESSAGE_KEY_HELLO + "\" locale=\"ja\"}}");
        String output = renderable.render(null, createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, "こんにちは");
    }

    @Test
    public void testLocaleFromParamWithMessageParams() {
        Model model = new MapModel(ImmutableMap.of("name", "ボブ"));
        HbsRenderable renderable = createRenderable(
                "{{i18n \"" + MESSAGE_KEY_HELLO_NAME + "\" @params.name locale=\"ja\"}}");
        String output = renderable.render(model, createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, "こんにちはボブ");
    }

    @Test
    public void testLocaleFromRequest() {
        RequestLookup requestLookup = createRequestLookup();
        HttpRequest request = requestLookup.getRequest();
        when(request.getHeaders()).thenReturn(ImmutableMap.of(HttpRequest.HEADER_ACCEPT_LANGUAGE, "fr,fr-FR;q=0.8"));

        HbsRenderable renderable = createRenderable("{{i18n \"" + MESSAGE_KEY_HELLO + "\"}}");
        String output = renderable.render(null, createLookup(), requestLookup, createAPI());
        Assert.assertEquals(output, "Bonjour");
    }

    @Test
    public void testLocaleFromConfiguration() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.other()).thenReturn(ImmutableMap.of("defaultLocale", "si"));
        Lookup lookup = createLookup();
        when(lookup.getConfiguration()).thenReturn(configuration);

        HbsRenderable renderable = createRenderable("{{i18n \"" + MESSAGE_KEY_HELLO + "\"}}");
        String output = renderable.render(null, lookup, createRequestLookup(), createAPI());
        Assert.assertEquals(output, "හෙලෝ");
    }

    @Test
    public void testFallbackLocale() {
        HbsRenderable renderable = createRenderable("{{i18n \"" + MESSAGE_KEY_HELLO + "\"}}");
        String output = renderable.render(null, createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, "Hello");
    }
}
