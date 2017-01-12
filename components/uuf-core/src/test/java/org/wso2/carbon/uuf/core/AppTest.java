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

package org.wso2.carbon.uuf.core;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.model.Model;

import static java.util.Collections.emptySet;
import static java.util.Collections.emptySortedSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for app.
 *
 * @since 1.0.0
 */
public class AppTest {

    private static Page createPage(String uri, String content) {
        return new Page(new UriPatten(uri), null, false) {
            @Override
            public String render(Model model, Lookup lookup, RequestLookup requestLookup, API api) {
                return content;
            }
        };
    }

    private static HttpRequest createRequest(String contextPath, String uriWithoutContextPath) {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getContextPath()).thenReturn(contextPath);
        when(request.getUri()).thenReturn(contextPath + uriWithoutContextPath);
        when(request.getUriWithoutContextPath()).thenReturn(uriWithoutContextPath);
        return request;
    }

    private static HttpResponse createResponse() {
        HttpResponse response = mock(HttpResponse.class, Mockito.RETURNS_DEEP_STUBS);
        return response;
    }

    @Test
    public void testRenderPage() {
        Page p1 = createPage("/a/b", "Page 1 content.");
        Page p2 = createPage("/x/y", "Page 2 content.");
        Component cmp1 = new Component("cmp1", null, "/cmp1", ImmutableSortedSet.of(p1, p2),
                                       emptySet(), emptySet(), emptySet(), null);
        Component rootComponent = new Component("root", null, Component.ROOT_COMPONENT_CONTEXT_PATH, emptySortedSet(),
                                                emptySet(), emptySet(), emptySet(), null);
        App app = new App(null, "/test", ImmutableSet.of(cmp1, rootComponent), emptySet(), new Configuration(), null,
                          null);
        String html = app.renderPage(createRequest("/test", "/cmp1/a/b"), createResponse());
        Assert.assertEquals(html, p1.render(null, null, null, null));
    }
}
