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
import org.wso2.carbon.uuf.core.API;

import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createAPI;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createLookup;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createRenderable;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createRequestLookup;

/**
 * Test cases for {@code {{#secured}}} helper.
 *
 * @since 1.0.0
 */
public class BlockSecuredHelperTest {

    @Test
    public void testWithoutPermission() {
        String template = "{{#secured}} secured content {{else}} un-secured content {{/secured}}";
        String output = createRenderable(template).render(null, createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, " un-secured content ");
    }

    @Test
    public void testWithPermission() {
        String template = "{{#secured \"resourceUri\" \"action\"}}" +
                "secured content" +
                "{{else}}" +
                "un-secured content" +
                "{{/secured}}";
        API api = createAPI();
        String output = createRenderable(template).render(null, createLookup(), createRequestLookup(), api);
        Assert.assertEquals(output, "un-secured content");
    }
}
