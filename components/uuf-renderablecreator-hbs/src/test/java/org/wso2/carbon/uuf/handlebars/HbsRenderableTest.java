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

package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.api.model.MapModel;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.Executable;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.HbsFragmentRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.HbsPageRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.model.ContextModel;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.model.Model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class HbsRenderableTest {

    private static HbsPageRenderable createPageRenderable(String sourceStr) {
        return new HbsPageRenderable(new StringTemplateSource("<test-source-page>", sourceStr));
    }

    private static HbsPageRenderable createPageRenderable(String sourceStr, Executable executable) {
        return new HbsPageRenderable(new StringTemplateSource("<test-source-page>", sourceStr), executable);
    }

    private static HbsFragmentRenderable createFragmentRenderable(String sourceStr) {
        return new HbsFragmentRenderable(new StringTemplateSource("<test-source-fragment>", sourceStr));
    }

    private static HbsFragmentRenderable createFragmentRenderable(String sourceStr, Executable executable) {
        return new HbsFragmentRenderable(new StringTemplateSource("<test-source-fragment>", sourceStr), executable);
    }

    private static Model createModel() {
        return new MapModel(Collections.emptyMap());
    }

    private static Lookup createLookup() {
        Lookup lookup = mock(Lookup.class);
        when(lookup.getConfiguration()).thenReturn(new Configuration(Collections.emptyMap()));
        return lookup;
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
    public void testPageTemplate() {
        final String templateContent = "A Plain Handlebars template of a page.";
        HbsPageRenderable pageRenderable = createPageRenderable(templateContent);

        String output = pageRenderable.render(createModel(), createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, templateContent);
    }

    @Test
    public void testPageTemplateWithModel() {
        HbsPageRenderable pageRenderable = createPageRenderable("Hello {{@params.name}}! Have a good day.");
        Model model = new MapModel(ImmutableMap.of("name", "Bob"));

        String output = pageRenderable.render(model, createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, "Hello Bob! Have a good day.");
    }

    @Test
    public void testPageTemplateWithExecutable() {
        Executable executable = (context, api) -> ImmutableMap.of("name", "Alice");
        HbsPageRenderable pageRenderable = createPageRenderable("Hello {{name}}! Have a good day.", executable);
        Model model = new MapModel(new HashMap<>());

        String output = pageRenderable.render(model, createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testFragmentTemplate() {
        final String templateContent = "A Plain Handlebars template of a fragment.";
        HbsFragmentRenderable fragmentRenderable = createFragmentRenderable(templateContent);

        String output = fragmentRenderable.render(createModel(), createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, templateContent);
    }

    @Test
    public void testFragmentTemplateWithModel() {
        final String templateContent = "Hello {{../name}} & {{@params.name}}! Have a good day.";
        HbsFragmentRenderable fragmentRenderable = createFragmentRenderable(templateContent);
        Context parentContext = Context.newContext(ImmutableMap.of("name", "Alice"));
        Model model = new ContextModel(parentContext, ImmutableMap.of("name", "Bob"));

        String output = fragmentRenderable.render(model, createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, "Hello Alice & Bob! Have a good day.");
    }

    @Test
    public void testFragmentTemplateWithExecutable() {
        Executable executable = (context, api) -> ImmutableMap.of("name", "Alice");
        HbsFragmentRenderable fragmentRenderable = createFragmentRenderable("Hello {{name}}! Have a good day.",
                                                                            executable);
        Model model = new MapModel(new HashMap<>());

        String output = fragmentRenderable.render(model, createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testFragmentInclude() {
        HbsPageRenderable pageRenderable = createPageRenderable("X {{fragment \"test-fragment\"}} Y");
        Fragment fragment = mock(Fragment.class);
        when(fragment.render(any(), any(), any(), any())).thenReturn("fragment content");
        Lookup lookup = createLookup();
        when(lookup.getFragmentIn(any(), eq("test-fragment"))).thenReturn(Optional.of(fragment));

        String output = pageRenderable.render(createModel(), lookup, createRequestLookup(), createAPI());
        Assert.assertEquals(output, "X fragment content Y");
    }

    @Test
    public void testFragmentBinding() {
        HbsPageRenderable pageRenderable = createPageRenderable("X {{defineZone \"test-zone\"}} Y");
        Lookup lookup = createLookup();
        Fragment pushedFragment = mock(Fragment.class);
        when(pushedFragment.render(any(), any(), any(), any())).thenReturn("fragment content");
        when(lookup.getBindings(any(), eq("test-zone"))).thenReturn(ImmutableList.of(pushedFragment));

        String output = pageRenderable.render(createModel(), lookup, createRequestLookup(), createAPI());
        Assert.assertEquals(output, "X fragment content Y");
    }

    @Test
    public void testZone() {
        HbsPageRenderable pageRenderable = createPageRenderable("X {{defineZone \"test-zone\"}} Y");
        Lookup lookup = createLookup();
        when(lookup.getBindings(anyString(), anyString())).thenReturn(Collections.emptyList());
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getZoneContent("test-zone")).thenReturn(Optional.of("zone content"));

        String output = pageRenderable.render(createModel(), lookup, requestLookup, createAPI());
        Assert.assertEquals(output, "X zone content Y");
    }
}
