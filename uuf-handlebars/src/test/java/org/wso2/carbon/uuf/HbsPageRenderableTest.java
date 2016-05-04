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

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.handlebars.Executable;
import org.wso2.carbon.uuf.handlebars.HbsPageRenderable;
import org.wso2.carbon.uuf.model.MapModel;
import org.wso2.carbon.uuf.model.Model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
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
        Model model = mock(Model.class);
        when(model.toMap()).thenReturn(Collections.emptyMap());
        return model;
    }

    private static ComponentLookup createLookup() {
        ComponentLookup lookup = mock(ComponentLookup.class);
        when(lookup.getConfigurations()).thenReturn(Collections.emptyMap());
        return lookup;
    }

    private static RequestLookup createRequestLookup() {
        RequestLookup requestLookup = mock(RequestLookup.class);
        when(requestLookup.getZoneContent(anyString())).thenReturn(Optional.<String>empty());
        when(requestLookup.getPlaceholderContents()).thenReturn(Collections.<String, String>emptyMap());
        when(requestLookup.getAppContext()).thenReturn("/myapp");
        return requestLookup;
    }

    @Test
    public void testTemplate() {
        final String templateContent = "A Plain Handlebars template.";
        HbsPageRenderable pageRenderable = createPageRenderable(templateContent);

        String output = pageRenderable.render(createModel(), createLookup(), createRequestLookup(), null);
        Assert.assertEquals(output, templateContent);
    }

    @Test
    public void testTemplateWithExecutable() {
        Executable executable = (context, api) -> ImmutableMap.of("name", "Alice");
        HbsPageRenderable pageRenderable = createPageRenderable("Hello {{name}}! Have a good day.", executable);
        Model model = new MapModel(new HashMap<>());

        String output = pageRenderable.render(model, createLookup(), createRequestLookup(), null);
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testFragmentInclude() {
        HbsPageRenderable pageRenderable = createPageRenderable("X {{includeFragment \"test-fragment\"}} Y");
        Fragment fragment = mock(Fragment.class);
        when(fragment.render(any(), any(), any(), any())).thenReturn("fragment content");
        ComponentLookup lookup = createLookup();
        when(lookup.getFragment("test-fragment")).thenReturn(Optional.of(fragment));

        String output = pageRenderable.render(createModel(), lookup, createRequestLookup(), null);
        Assert.assertEquals(output, "X fragment content Y");
    }

    @Test
    public void testFragmentBinding() {
        HbsPageRenderable pageRenderable = createPageRenderable("X {{defineZone \"test-zone\"}} Y");
        ComponentLookup lookup = createLookup();
        Fragment pushedFragment = mock(Fragment.class);
        when(pushedFragment.render(any(), any(), any(), any())).thenReturn("fragment content");
        when(lookup.getBindings("test-zone")).thenReturn(ImmutableSet.of(pushedFragment));

        String output = pageRenderable.render(createModel(), lookup, createRequestLookup(), null);
        Assert.assertEquals(output, "X fragment content Y");
    }

    @Test
    public void testZone() {
        HbsPageRenderable pageRenderable = createPageRenderable("X {{defineZone \"test-zone\"}} Y");
        ComponentLookup lookup = createLookup();
        when(lookup.getBindings(anyString())).thenReturn(Collections.emptySet());
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getZoneContent("test-zone")).thenReturn(Optional.of("zone content"));

        String output = pageRenderable.render(createModel(), lookup, requestLookup, null);
        Assert.assertEquals(output, "X zone content Y");
    }

    @Test
    public void testPublicHelper() {
        final String templateContent = "{{public \"/relative/path\"}}";
        HbsPageRenderable pageRenderable = createPageRenderable(templateContent);
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/mycomponent/base");

        String output = pageRenderable.render(createModel(), createLookup(), requestLookup, null);
        Assert.assertEquals(output, "/myapp/public/mycomponent/base/relative/path");
    }
}
