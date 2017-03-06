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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.spi.HttpRequest;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for RequestLookup.
 *
 * @since 1.0.0
 */
public class RequestLookupTest {

    @Test
    public void testContextPath() {
        RequestLookup requestLookup = new RequestLookup("/test1", null, null);
        Assert.assertEquals(requestLookup.getContextPath(), "/test1");

        HttpRequest request = mock(HttpRequest.class);
        when(request.getContextPath()).thenReturn("/test2");
        requestLookup = new RequestLookup(null, request, null);
        Assert.assertEquals(requestLookup.getContextPath(), "/test2");
    }

    @Test
    public void testPlaceholder() {
        RequestLookup requestLookup = new RequestLookup("/test", null, null);

        final String css1 = "<some css link>";
        requestLookup.addToPlaceholder(Placeholder.css, css1);
        Assert.assertTrue(requestLookup.getPlaceholderContent(Placeholder.css).isPresent());
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.css).orElse(null), css1);
        Assert.assertFalse(requestLookup.getPlaceholderContent(Placeholder.js).isPresent());

        final String css2 = "<another css link>";
        requestLookup.addToPlaceholder(Placeholder.css, css2);
        Assert.assertTrue(requestLookup.getPlaceholderContent(Placeholder.css).isPresent());
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.css).orElse(null), css1 + css2);
        Assert.assertFalse(requestLookup.getPlaceholderContent(Placeholder.js).isPresent());

        final String js = "<some js link>";
        requestLookup.addToPlaceholder(Placeholder.js, js);
        Assert.assertTrue(requestLookup.getPlaceholderContent(Placeholder.js).isPresent());
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.js).orElse(null), js);
        Assert.assertFalse(requestLookup.getPlaceholderContent(Placeholder.headJs).isPresent());

        Map<String, String> placeholderContents = requestLookup.getPlaceholderContents();
        Assert.assertEquals(placeholderContents.get(Placeholder.css.name()), css1 + css2);
        Assert.assertEquals(placeholderContents.get(Placeholder.js.name()), js);
        Assert.assertEquals(placeholderContents.get(Placeholder.headJs.name()), null);
    }

    @Test
    public void testPathParams() {
        RequestLookup requestLookup = new RequestLookup("/test", null, null);

        Map<String, String> pathParams = Collections.singletonMap("key", "value");
        requestLookup.setPathParams(pathParams);
        Assert.assertEquals(requestLookup.getPathParams(), pathParams);
    }

    @Test
    public void testZoneContent() {
        RequestLookup requestLookup = new RequestLookup("/test", null, null);

        requestLookup.putToZone("z1", "content of z1");
        requestLookup.putToZone("z2", "content of z2");
        requestLookup.putToZone("z3", "content of z3");

        Assert.assertEquals(requestLookup.getZoneContent("z1").orElse(null), "content of z1");
    }

    @Test
    public void testPublicUri() {
        RequestLookup requestLookup = new RequestLookup("/test", null, null);

        final String publicUri1 = "/public/components/component1/base";
        requestLookup.pushToPublicUriStack(publicUri1);
        Assert.assertEquals(requestLookup.getPublicUri(), "/test" + publicUri1);
        final String publicUri2 = "/public/components/component2/base";
        requestLookup.pushToPublicUriStack(publicUri2);
        Assert.assertEquals(requestLookup.getPublicUri(), "/test" + publicUri2);
        requestLookup.popPublicUriStack();
        Assert.assertEquals(requestLookup.getPublicUri(), "/test" + publicUri1);
    }
}
