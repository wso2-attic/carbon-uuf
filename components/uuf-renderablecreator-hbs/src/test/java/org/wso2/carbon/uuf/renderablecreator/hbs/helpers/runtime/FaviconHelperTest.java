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

import com.github.jknack.handlebars.HandlebarsException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.RequestLookup;

import static org.mockito.Mockito.when;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createAPI;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createLookup;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createRenderable;

/**
 * Test cases for the {@code {{favicon}}} helper.
 *
 * @since 1.0.0
 */
public class FaviconHelperTest {

    @Test
    public void test() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{favicon \"img/favicon.png\" type=\"image/png\"}}")
                .render(null, createLookup(), requestLookup, createAPI());
        String faviconUrl = "<link rel=\"shortcut icon\" href=\"/myapp/public/component/base/img/favicon.png\" " +
                "type=\"image/png\" />\n";

        String output = createRenderable("X {{placeholder \"favicon\"}} Y")
                .render(null, createLookup(), requestLookup, createAPI());
        Assert.assertEquals(output, "X " + faviconUrl + " Y");
    }

    @Test
    public void testWithDefault() {
        String layoutTemplate = "X" +
                "{{#placeholder \"favicon\"}}" +
                " <link rel=\"shortcut icon\" href=\"{{public \"img/default-favicon.png\"}}\" type=\"image/png\"/>" +
                "{{/placeholder}}" +
                " Y";
        String faviconUrl =
                "<link rel=\"shortcut icon\" href=\"/myapp/public/component/base/img/default-favicon.png\" " +
                        "type=\"image/png\"/>";

        String output = createRenderable(layoutTemplate).render(null, createLookup(), createRequestLookup(),
                                                                createAPI());
        Assert.assertEquals(output, "X " + faviconUrl + " Y");
    }

    @Test
    public void testValidation() {
        try {
            createRenderable("{{favicon null}}").render(null, createLookup(), createRequestLookup(), createAPI());
            Assert.fail("{{favicon}} helper accepts null parameters!");
        } catch (HandlebarsException e) {
            Assert.assertTrue((e.getCause() instanceof IllegalArgumentException),
                              "Cause of the thrown exception should be '" + IllegalArgumentException.class +
                                      "'. Instead found '" + e.getCause().getClass() + "'.");
        }
    }

    private static RequestLookup createRequestLookup() {
        RequestLookup requestLookup = RuntimeHelpersTestUtil.createRequestLookup();
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/component/base");
        return requestLookup;
    }
}
