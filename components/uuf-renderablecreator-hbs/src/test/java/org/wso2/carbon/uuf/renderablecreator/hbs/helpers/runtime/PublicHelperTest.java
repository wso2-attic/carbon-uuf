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
 * Test cases for {@code {{public}}} helper.
 *
 * @since 1.0.0
 */
public class PublicHelperTest {

    @Test
    public void test() {
        String template = "<img src=\"{{public \"img/my-photo.png\"}}\" />";
        String output = createRenderable(template).render(null, createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, "<img src=\"/myapp/public/component/base/img/my-photo.png\" />");

    }

    @Test
    public void testWithMultipleParameters() {
        String template = "<img src=\"{{public \"img/\" \"my-photo.png\"}}\" />";
        String output = createRenderable(template).render(null, createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, "<img src=\"/myapp/public/component/base/img/my-photo.png\" />");

    }

    @Test
    public void testValidation() {
        try {
            createRenderable("{{public null}}").render(null, createLookup(), createRequestLookup(), createAPI());
            Assert.fail("{{public}} helper accepts null parameters!");
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
