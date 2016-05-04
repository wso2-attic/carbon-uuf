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
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.RequestLookup;

import java.util.Collections;

import static org.mockito.Matchers.any;

public class PageTest {

    @Test
    public void testRenderPage() {
        final String content = "Hello world from a page!";
        Renderable renderable = (model, componentLookup, requestLookup, api) -> content;
        ComponentLookup lookup = new ComponentLookup("componentName", "/componentContext", Collections.emptySet(),
                                                     Collections.emptySet(), ImmutableSetMultimap.of(),
                                                     Collections.emptyMap(), Collections.emptySet());
        RequestLookup requestLookup = new RequestLookup("/appContext", any());
        Page page = new Page(any(), renderable);

        String output = page.render(any(), lookup, requestLookup, any());
        Assert.assertEquals(output, content);
    }
}
