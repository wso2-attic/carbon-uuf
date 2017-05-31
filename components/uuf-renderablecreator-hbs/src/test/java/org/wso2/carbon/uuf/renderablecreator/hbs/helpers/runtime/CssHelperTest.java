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
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.exception.HbsRenderingException;

import static org.mockito.Mockito.when;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createAPI;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createLookup;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createRenderable;

/**
 * Test cases for {@code {{css}}} helper.
 *
 * @since 1.0.0
 */
public class CssHelperTest {

    @Test
    public void test() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{css \"css/my-styles.css\"}}")
                .render(null, createLookup(), requestLookup, createAPI());
        String cssUrl = "<link href=\"/myapp/public/component/base/css/my-styles.css\" rel=\"stylesheet\" " +
                "type=\"text/css\" />\n";

        String output = createRenderable("X {{placeholder \"css\"}} Y")
                .render(null, createLookup(), requestLookup, createAPI());
        Assert.assertEquals(output, "X " + cssUrl + " Y");
    }

    @Test
    public void testWithMultipleParameters() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{css \"css/\" \"my-styles\" \".css\"}}")
                .render(null, createLookup(), requestLookup, createAPI());
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.css).orElse(null),
                            "<link href=\"/myapp/public/component/base/css/my-styles.css\" rel=\"stylesheet\" " +
                                    "type=\"text/css\" />\n");
    }

    @Test
    public void testWhenSameCssAddedTwice() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{css \"css/my-styles.css\"}} {{css \"css/my-styles.css\"}}")
                .render(null, createLookup(), requestLookup, createAPI());
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.css).orElse(null),
                            "<link href=\"/myapp/public/component/base/css/my-styles.css\" rel=\"stylesheet\" " +
                                    "type=\"text/css\" />\n");
    }

    @Test
    public void testValidation() {
        Assert.assertThrows(HbsRenderingException.class,
                            () -> createRenderable("{{css null}}")
                                    .render(null, createLookup(), createRequestLookup(), createAPI()));
    }

    private static RequestLookup createRequestLookup() {
        RequestLookup requestLookup = RuntimeHelpersTestUtil.createRequestLookup();
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/component/base");
        RequestLookup.RenderingFlowTracker tracker = RuntimeHelpersTestUtil.createRenderingFlowTracker(false);
        when(tracker.getCurrentComponentName()).thenReturn("test.component");
        when(requestLookup.tracker()).thenReturn(tracker);
        return requestLookup;
    }
}
