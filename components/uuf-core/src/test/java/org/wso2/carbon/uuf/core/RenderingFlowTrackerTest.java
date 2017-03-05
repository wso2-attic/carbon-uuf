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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for RenderingFlowTracker.
 *
 * @since 1.0.0
 */
public class RenderingFlowTrackerTest {

    @Test
    public void testRenderingTracker() {
        RequestLookup requestLookup = new RequestLookup("/test", null, null);

        Assert.assertFalse(requestLookup.tracker().isInPage());
        Assert.assertFalse(requestLookup.tracker().isInFragment());
        Assert.assertFalse(requestLookup.tracker().isInLayout());
        Assert.assertFalse(requestLookup.tracker().getCurrentPage().isPresent());
        Assert.assertFalse(requestLookup.tracker().getCurrentFragment().isPresent());
        Assert.assertFalse(requestLookup.tracker().getCurrentLayout().isPresent());

        Component component = mock(Component.class);
        when(component.getName()).thenReturn("test.component");
        requestLookup.tracker().start(component);
        Assert.assertEquals(requestLookup.tracker().getCurrentComponentName(), "test.component");
        Assert.assertFalse(requestLookup.tracker().isInPage());
        Assert.assertFalse(requestLookup.tracker().isInFragment());
        Assert.assertFalse(requestLookup.tracker().isInLayout());
        Assert.assertFalse(requestLookup.tracker().getCurrentPage().isPresent());
        Assert.assertFalse(requestLookup.tracker().getCurrentFragment().isPresent());
        Assert.assertFalse(requestLookup.tracker().getCurrentLayout().isPresent());

        Page page = mock(Page.class);
        requestLookup.tracker().in(page);
        Assert.assertTrue(requestLookup.tracker().isInPage());
        Assert.assertFalse(requestLookup.tracker().isInFragment());
        Assert.assertFalse(requestLookup.tracker().isInLayout());
        Assert.assertEquals(requestLookup.tracker().getCurrentPage().orElse(null), page);
        Assert.assertFalse(requestLookup.tracker().getCurrentFragment().isPresent());
        Assert.assertFalse(requestLookup.tracker().getCurrentLayout().isPresent());

        Fragment fragment1 = mock(Fragment.class);
        when(fragment1.getName()).thenReturn("test.component1.fragment1");
        requestLookup.tracker().in(fragment1);
        Assert.assertEquals(requestLookup.tracker().getCurrentComponentName(), "test.component1");
        Assert.assertTrue(requestLookup.tracker().isInPage());
        Assert.assertTrue(requestLookup.tracker().isInFragment());
        Assert.assertFalse(requestLookup.tracker().isInLayout());
        Assert.assertEquals(requestLookup.tracker().getCurrentPage().orElse(null), page);
        Assert.assertEquals(requestLookup.tracker().getCurrentFragment().orElse(null), fragment1);
        Assert.assertFalse(requestLookup.tracker().getCurrentLayout().isPresent());

        Fragment fragment2 = mock(Fragment.class);
        when(fragment2.getName()).thenReturn("test.component2.fragment2");
        requestLookup.tracker().in(fragment2);
        Assert.assertEquals(requestLookup.tracker().getCurrentComponentName(), "test.component2");
        Assert.assertTrue(requestLookup.tracker().isInPage());
        Assert.assertTrue(requestLookup.tracker().isInFragment());
        Assert.assertFalse(requestLookup.tracker().isInLayout());
        Assert.assertEquals(requestLookup.tracker().getCurrentPage().orElse(null), page);
        Assert.assertEquals(requestLookup.tracker().getCurrentFragment().orElse(null), fragment2);
        Assert.assertFalse(requestLookup.tracker().getCurrentLayout().isPresent());

        requestLookup.tracker().out(fragment2);
        Assert.assertEquals(requestLookup.tracker().getCurrentComponentName(), "test.component1");
        Assert.assertTrue(requestLookup.tracker().isInFragment());
        Assert.assertEquals(requestLookup.tracker().getCurrentFragment().orElse(null), fragment1);

        requestLookup.tracker().out(fragment1);
        Assert.assertEquals(requestLookup.tracker().getCurrentComponentName(), "test.component");
        Assert.assertFalse(requestLookup.tracker().isInFragment());

        Assert.assertThrows(IllegalStateException.class, () -> requestLookup.tracker().out(fragment1));

        Layout layout = mock(Layout.class);
        when(layout.getName()).thenReturn("test.component3.layout");
        requestLookup.tracker().in(layout);
        Assert.assertEquals(requestLookup.tracker().getCurrentComponentName(), "test.component3");
        Assert.assertTrue(requestLookup.tracker().isInPage());
        Assert.assertFalse(requestLookup.tracker().isInFragment());
        Assert.assertTrue(requestLookup.tracker().isInLayout());
        Assert.assertEquals(requestLookup.tracker().getCurrentPage().orElse(null), page);
        Assert.assertFalse(requestLookup.tracker().getCurrentFragment().isPresent());
        Assert.assertEquals(requestLookup.tracker().getCurrentLayout().orElse(null), layout);

        requestLookup.tracker().out(layout);
        Assert.assertEquals(requestLookup.tracker().getCurrentComponentName(), "test.component");
        Assert.assertFalse(requestLookup.tracker().isInLayout());

        Assert.assertThrows(IllegalStateException.class, () -> requestLookup.tracker().out(layout));

        requestLookup.tracker().out(page);
        Assert.assertFalse(requestLookup.tracker().isInPage());

        Assert.assertThrows(IllegalStateException.class, () -> requestLookup.tracker().out(page));

        requestLookup.tracker().finish();
        Assert.assertFalse(requestLookup.tracker().isInPage());
        Assert.assertFalse(requestLookup.tracker().isInFragment());
        Assert.assertFalse(requestLookup.tracker().isInLayout());
        Assert.assertFalse(requestLookup.tracker().getCurrentPage().isPresent());
        Assert.assertFalse(requestLookup.tracker().getCurrentFragment().isPresent());
        Assert.assertFalse(requestLookup.tracker().getCurrentLayout().isPresent());
    }
}
