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

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedSet;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.Configuration;
import org.wso2.carbon.uuf.api.HttpRequest;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.internal.core.UriPatten;
import org.wso2.carbon.uuf.spi.model.Model;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComponentTest {

    private static Page createPage(String uriPattern, String content) {
        return new Page(new UriPatten(uriPattern), null, false) {
            @Override
            public String render(Model model, ComponentLookup lookup, RequestLookup requestLookup, API api) {
                return content;
            }
        };
    }

    private static ComponentLookup createLookup(String componentName) {
        return new ComponentLookup(componentName, "/componentContext", Collections.emptySet(), Collections.emptySet(),
                                   ImmutableSetMultimap.of(), Configuration.emptyConfiguration(),
                                   Collections.emptySet());
    }

    private static RequestLookup createRequestLookup() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getAppContext()).thenReturn("/appContext");
        return (new RequestLookup(request));
    }

    @Test
    public void testRenderExistingPage() {
        Page p1 = createPage("/test/page/one", "Hello world from test page one!");
        Page p2 = createPage("/test/page/two", "Hello world from test page two!");
        ComponentLookup lookup = createLookup("componentName");
        Component component = new Component("componentName", "1.0.0", ImmutableSortedSet.of(p1, p2), lookup);

        Optional<String> output = component.renderPage("/test/page/one", createRequestLookup(), null);
        Assert.assertEquals(output.get(), "Hello world from test page one!");
    }

    @Test
    public void testRenderExistingPageWithWildcard() {
        Page p1 = createPage("/test/page/{wildcard}/one", "Hello world from test page one!");
        Page p2 = createPage("/test/page/no-wildcard/two", "Hello world from test page two!");
        ComponentLookup lookup = createLookup("componentName");
        Component component = new Component("componentName", "1.0.0", ImmutableSortedSet.of(p1, p2), lookup);

        Optional<String> output = component.renderPage("/test/page/wildcard-value/one", createRequestLookup(), null);
        Assert.assertEquals(output.get(), "Hello world from test page one!");
    }

    @Test
    public void testRenderNonExistingPage() {
        Page p1 = createPage("/test/page/one", null);
        Page p2 = createPage("/test/page/two", null);
        ComponentLookup lookup = createLookup("componentName");
        Component component = new Component("componentName", "1.0.0", ImmutableSortedSet.of(p1, p2), lookup);

        Optional<String> output = component.renderPage("/test/page/three", createRequestLookup(), null);
        Assert.assertFalse(output.isPresent());
    }
}
