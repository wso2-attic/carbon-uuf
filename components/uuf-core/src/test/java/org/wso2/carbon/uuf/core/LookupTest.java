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

import com.google.common.collect.ImmutableSet;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Set;

/**
 * Test cases for API.
 *
 * @since 1.0.0
 */
public class LookupTest {

    private static Fragment createFragment(String name) {
        return new Fragment(name, null, false);
    }

    private static Component createComponent(String name, Set<Fragment> fragments, Set<Component> dependencies) {
        return new Component(name, null, null, Collections.emptySortedSet(), fragments, Collections.emptySet(),
                             dependencies, null);
    }

    @Test
    public void testLookup() {
        Component c1 = createComponent("c1", ImmutableSet.of(createFragment("c1.f1"), createFragment("c1.f2")),
                                       Collections.emptySet());
        Component c2 = createComponent("c2", ImmutableSet.of(createFragment("c2.f1"), createFragment("c2.f2")),
                                       ImmutableSet.of(c1));
        Component c3 = createComponent("c3", ImmutableSet.of(createFragment("c3.f1"), createFragment("c3.f2")),
                                       Collections.emptySet());
        Component root = createComponent("root", ImmutableSet.of(createFragment("root.f1")), ImmutableSet.of(c2, c3));
        Lookup lookup = new Lookup(ImmutableSet.of(c1, c2, c3, root), null, null, null);

        // accessing root's fragments
        Assert.assertEquals(lookup.getFragmentIn("root", "f1").get().getName(), "root.f1");
        Assert.assertEquals(lookup.getFragmentIn("root", "root.f1").get().getName(), "root.f1");
        // accessing root's immediate dependencies' fragments
        Assert.assertEquals(lookup.getFragmentIn("root", "c2.f2").get().getName(), "c2.f2");
        Assert.assertEquals(lookup.getFragmentIn("root", "c3.f1").get().getName(), "c3.f1");
        // accessing root's transitive dependencies' fragments
        Assert.assertEquals(lookup.getFragmentIn("root", "c1.f1").get().getName(), "c1.f1");
        // accessing non-existing fragment's through root
        Assert.assertEquals(lookup.getFragmentIn("root", "foo").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("root", "root.foo").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("root", "c2.foo").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("root", "c3.foo").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("root", "c1.foo").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("root", "c0.foo").isPresent(), false);

        // accessing c2's fragments
        Assert.assertEquals(lookup.getFragmentIn("c2", "f1").get().getName(), "c2.f1");
        Assert.assertEquals(lookup.getFragmentIn("c2", "f2").get().getName(), "c2.f2");
        // accessing c2's immediate dependencies' fragments
        Assert.assertEquals(lookup.getFragmentIn("c2", "c1.f1").get().getName(), "c1.f1");
        Assert.assertEquals(lookup.getFragmentIn("c2", "c1.f2").get().getName(), "c1.f2");
        // accessing non-existing fragment's through root
        Assert.assertEquals(lookup.getFragmentIn("c2", "foo").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("c2", "c1.foo").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("c2", "root.foo").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("c2", "c0.foo").isPresent(), false);

        // accessing c3's fragments
        Assert.assertEquals(lookup.getFragmentIn("c3", "f1").get().getName(), "c3.f1");
        Assert.assertEquals(lookup.getFragmentIn("c3", "f2").get().getName(), "c3.f2");
        // accessing non-existing fragments through c3
        Assert.assertEquals(lookup.getFragmentIn("c3", "foo").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("c3", "c3.f00").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("c3", "root.f00").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("c3", "c0.f00").isPresent(), false);

        // accessing c1's fragments
        Assert.assertEquals(lookup.getFragmentIn("c1", "f1").get().getName(), "c1.f1");
        Assert.assertEquals(lookup.getFragmentIn("c1", "f2").get().getName(), "c1.f2");
        // accessing non-existing fragments through c1
        Assert.assertEquals(lookup.getFragmentIn("c1", "foo").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("c1", "c1.foo").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("c1", "c2.foo").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("c1", "root.foo").isPresent(), false);
        Assert.assertEquals(lookup.getFragmentIn("c1", "c0.foo").isPresent(), false);
    }
}
