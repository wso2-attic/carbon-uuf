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
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.spi.Renderable;

import java.util.Collections;

public class FragmentTest {

    @Test
    public void testRenderFragment() {
        final String content = "Hello world from a fragment!";
        Renderable renderable = (model, componentLookup, requestLookup, api) -> content;
        Component component = new Component("componentName", null, "/componentContext", Collections.emptySortedSet());
        Lookup lookup = new Lookup(ImmutableSetMultimap.of());
        lookup.add(component);
        RequestLookup requestLookup = new RequestLookup("/appContext", null, null);
        Fragment fragment = new Fragment("componentName.fragmentName", renderable, false);

        String output = fragment.render(null, lookup, requestLookup, null);
        Assert.assertEquals(output, content);
    }
}
