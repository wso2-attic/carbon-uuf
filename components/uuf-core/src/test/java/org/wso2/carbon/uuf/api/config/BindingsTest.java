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

package org.wso2.carbon.uuf.api.config;

import com.google.common.collect.ImmutableList;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.Fragment;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Test cases for bindings.
 *
 * @since 1.0.0
 */
public class BindingsTest {

    private static Bindings createBindings() {
        return new Bindings();
    }

    private static Fragment createFragment(String name) {
        return new Fragment(name, null, null);
    }

    @Test
    public void testAddBindingValidation() {
        Bindings bindings = createBindings();

        Assert.assertThrows(IllegalArgumentException.class,
                            () -> bindings.addBinding(null, emptyList(), Bindings.Mode.prepend));
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> bindings.addBinding("", emptyList(), Bindings.Mode.append));
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> bindings.addBinding("abc", emptyList(), Bindings.Mode.overwrite));

        Assert.assertThrows(IllegalArgumentException.class,
                            () -> bindings.addBinding("org.wso2.foo.z1", null, Bindings.Mode.prepend));
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> bindings.addBinding("org.wso2.foo.z1", singletonList(null), Bindings.Mode.append));

        Assert.assertThrows(IllegalArgumentException.class,
                            () -> bindings.addBinding("org.wso2.foo.z1", emptyList(), null));
    }

    @Test
    public void testBindingsMerge() {
        Bindings bindings = createBindings();
        String zoneName = "org.wso2.foo.z1";

        List<Fragment> fragments1 = ImmutableList.of(createFragment("org.wso2.foo.f1"),
                                                     createFragment("org.wso2.foo.f2"));
        bindings.addBinding(zoneName, fragments1, Bindings.Mode.prepend);
        Assert.assertEquals(bindings.getBindings(zoneName), fragments1);
        // prepend mode
        List<Fragment> fragments2 = ImmutableList.of(createFragment("org.wso2.foo.f3"),
                                                     createFragment("org.wso2.foo.f4"));
        bindings.addBinding(zoneName, fragments2, Bindings.Mode.prepend);
        Assert.assertEquals(bindings.getBindings(zoneName), ImmutableList.of(createFragment("org.wso2.foo.f3"),
                                                                             createFragment("org.wso2.foo.f4"),
                                                                             createFragment("org.wso2.foo.f1"),
                                                                             createFragment("org.wso2.foo.f2")));
        // append mode
        List<Fragment> fragments3 = ImmutableList.of(createFragment("org.wso2.foo.f5"),
                                                     createFragment("org.wso2.foo.f6"));
        bindings.addBinding(zoneName, fragments3, Bindings.Mode.append);
        Assert.assertEquals(bindings.getBindings(zoneName), ImmutableList.of(createFragment("org.wso2.foo.f3"),
                                                                             createFragment("org.wso2.foo.f4"),
                                                                             createFragment("org.wso2.foo.f1"),
                                                                             createFragment("org.wso2.foo.f2"),
                                                                             createFragment("org.wso2.foo.f5"),
                                                                             createFragment("org.wso2.foo.f6")));
        // overwrite mode
        List<Fragment> fragments4 = ImmutableList.of(createFragment("org.wso2.foo.f7"),
                                                     createFragment("org.wso2.foo.f8"));
        bindings.addBinding(zoneName, fragments4, Bindings.Mode.overwrite);
        Assert.assertEquals(bindings.getBindings(zoneName), ImmutableList.of(createFragment("org.wso2.foo.f7"),
                                                                             createFragment("org.wso2.foo.f8")));
        // another zone's bindings
        Assert.assertEquals(bindings.getBindings("org.wso2.foo.z2").size(), 0);
    }
}
