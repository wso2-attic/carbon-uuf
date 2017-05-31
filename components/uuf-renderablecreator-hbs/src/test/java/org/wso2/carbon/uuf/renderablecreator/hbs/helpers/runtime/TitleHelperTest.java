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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.exception.HbsRenderingException;

import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createAPI;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createLookup;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createRenderable;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createRequestLookup;

/**
 * Test cases for the {@code {{title}}} helper.
 *
 * @since 1.0.0
 */
public class TitleHelperTest {

    @Test
    public void test() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{title \"Some Title\"}}")
                .render(null, createLookup(), requestLookup, createAPI());
        String pageTitle = "Some Title";

        String output = createRenderable("X {{placeholder \"title\"}} Y")
                .render(null, createLookup(), requestLookup, createAPI());
        Assert.assertEquals(output, "X " + pageTitle + " Y");
    }

    @Test
    public void testWithDefault() {
        String layoutTemplate = "X" +
                "{{#placeholder \"title\"}}" +
                " Some Default Title" +
                "{{/placeholder}}" +
                " Y";
        String pageTitle = "Some Default Title";

        String output = createRenderable(layoutTemplate)
                .render(null, createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, "X " + pageTitle + " Y");
    }

    @Test
    public void testSettingMultiple() {
        String pageTemplate = "{{title \"Some Title\"}} bla bla {{title \"Another Title\"}}";
        Assert.assertThrows(HbsRenderingException.class,
                            () -> createRenderable(pageTemplate)
                                    .render(null, createLookup(), createRequestLookup(), createAPI()));
    }

    @Test
    public void testValidation() {
        Assert.assertThrows(HbsRenderingException.class,
                            () -> createRenderable("{{title null}}")
                                    .render(null, createLookup(), createRequestLookup(), createAPI()));
    }
}
