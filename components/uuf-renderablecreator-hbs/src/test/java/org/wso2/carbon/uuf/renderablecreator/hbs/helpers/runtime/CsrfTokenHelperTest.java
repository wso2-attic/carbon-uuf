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
import org.wso2.carbon.uuf.spi.HttpRequest;

import static org.mockito.Mockito.when;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createAPI;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createLookup;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createRenderable;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createRequestLookup;
import static org.wso2.carbon.uuf.spi.HttpRequest.COOKIE_CSRFTOKEN;

/**
 * Test cases for {@code {{csrfToken}}} helper.
 *
 * @since 1.0.0
 */
public class CsrfTokenHelperTest {

    @Test
    public void testCSRFTokenHelper() {
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getRequest().getCookieValue(COOKIE_CSRFTOKEN))
                .thenReturn("A45B3DDE4CF00891E7A9F3B752F18F92");

        String output = createRenderable("{{csrfToken}}").render(null, createLookup(), requestLookup, createAPI());
        Assert.assertEquals(output, "<input type=\"hidden\" name=\"" + HttpRequest.COOKIE_CSRFTOKEN +
                "\" value=\"A45B3DDE4CF00891E7A9F3B752F18F92\"/>");
    }
}
