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

import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createAPI;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createLookup;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createRenderable;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createRequestLookup;

/**
 * Test cases for {@code {{headOther}}} helper.
 *
 * @since 1.0.0
 */
public class HeadOtherHelperTest {

    @Test
    public void testHeadOther() {
        String content = "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">";
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{#headOther}}" + content + "{{/headOther}}")
                .render(null, createLookup(), requestLookup, createAPI());

        String output = createRenderable("X {{placeholder \"headOther\"}} Y")
                .render(null, createLookup(), requestLookup, createAPI());
        Assert.assertEquals(output, "X " + content + " Y");
    }
}
