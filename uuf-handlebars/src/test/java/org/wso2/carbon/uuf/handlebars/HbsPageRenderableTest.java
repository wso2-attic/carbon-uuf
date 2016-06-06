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

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.Configuration;
import org.wso2.carbon.uuf.api.HttpRequest;
import org.wso2.carbon.uuf.api.model.MapModel;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.handlebars.renderable.HbsPageRenderable;
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

public class HbsPageRenderableTest {

    private static HbsPageRenderable createPageRenderable(String sourceStr) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsPageRenderable(stringTemplateSource);
    }

    private static HbsPageRenderable createPageRenderable(String sourceStr, Executable executable) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsPageRenderable(stringTemplateSource, executable);
    }

    private static Model createModel() {
        return new MapModel(Collections.emptyMap());
    }

    private static Lookup createLookup() {
        Lookup lookup = mock(Lookup.class);
        when(lookup.getConfiguration()).thenReturn(Configuration.emptyConfiguration());
        return lookup;
    }

    private static RequestLookup createRequestLookup() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getQueryParams()).thenReturn(Collections.emptyMap());
        return spy(new RequestLookup("/appContext", request, null));
    }

    private static API createAPI() {
        API api = mock(API.class);
        when(api.getSession()).thenReturn(Optional.empty());
        return api;
    }

    @Test
    public void testTemplate() {
        final String templateContent = "A Plain Handlebars template.";
        HbsPageRenderable pageRenderable = createPageRenderable(templateContent);

        String output = pageRenderable.render(createModel(), createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, templateContent);
    }

    @Test
    public void testTemplateWithExecutable() {
        Executable executable = (context, api) -> ImmutableMap.of("name", "Alice");
        HbsPageRenderable pageRenderable = createPageRenderable("Hello {{name}}! Have a good day.", executable);
        Model model = new MapModel(new HashMap<>());

        String output = pageRenderable.render(model, createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testFragmentInclude() {
        HbsPageRenderable pageRenderable = createPageRenderable("X {{fragment \"test-fragment\"}} Y");
        Fragment fragment = mock(Fragment.class);
        when(fragment.render(any(), any(), any(), any())).thenReturn("fragment content");
        Lookup lookup = createLookup();
        when(lookup.getFragmentIn(any(), anyString())).thenReturn(Optional.of(fragment));

        String output = pageRenderable.render(createModel(), lookup, createRequestLookup(), createAPI());
        Assert.assertEquals(output, "X fragment content Y");
    }

    @Test
    public void testFragmentBinding() {
        HbsPageRenderable pageRenderable = createPageRenderable("X {{defineZone \"test-zone\"}} Y");
        Lookup lookup = createLookup();
        Fragment pushedFragment = mock(Fragment.class);
        when(pushedFragment.render(any(), any(), any(), any())).thenReturn("fragment content");
        when(lookup.getBindings(any(), eq("test-zone"))).thenReturn(ImmutableSet.of(pushedFragment));

        String output = pageRenderable.render(createModel(), lookup, createRequestLookup(), createAPI());
        Assert.assertEquals(output, "X fragment content Y");
    }

    @Test
    public void testZone() {
        HbsPageRenderable pageRenderable = createPageRenderable("X {{defineZone \"test-zone\"}} Y");
        Lookup lookup = createLookup();
        when(lookup.getBindings(anyString(), anyString())).thenReturn(Collections.emptySet());
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getZoneContent("test-zone")).thenReturn(Optional.of("zone content"));

        String output = pageRenderable.render(createModel(), lookup, requestLookup, createAPI());
        Assert.assertEquals(output, "X zone content Y");
    }
}
