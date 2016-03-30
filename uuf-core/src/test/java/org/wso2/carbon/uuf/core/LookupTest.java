package org.wso2.carbon.uuf.core;

import com.google.common.collect.ImmutableSet;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LookupTest {

    @Test
    public void testLookupLocalFragment() throws Exception {
        Fragment myFragment = mock(Fragment.class);
        when(myFragment.getName()).thenReturn("org.wso2.test.myFragment");

        Lookup lookup = new Lookup(
                "org.wso2.test",
   /*bindings*/ Collections.emptyMap(),
                ImmutableSet.of(myFragment),
   /*children*/ Collections.emptySet());
        Fragment fragment = lookup.lookupFragment("org.wso2.test.myFragment");
        Assert.assertEquals(fragment, myFragment);

    }

    @Test
    public void testChildFragment() throws Exception {
        Fragment childFragment = mock(Fragment.class);
        when(childFragment.getName()).thenReturn("org.wso2.test.child.my-fragment");

        Component childComponent = mock(Component.class);
        Lookup childLookup = new Lookup(
                "org.wso2.test.child.my-fragment",
   /*bindings*/ Collections.emptyMap(),
                ImmutableSet.of(childFragment),
   /*children*/ Collections.emptySet());
        when(childComponent.getLookup()).thenReturn(childLookup);

        Lookup lookup = new Lookup(
                "org.wso2.test.parent",
   /*bindings*/ Collections.emptyMap(),
  /*fragments*/ Collections.emptySet(),
   /*children*/ Collections.singleton(childComponent));

        Fragment fragment = lookup.lookupFragment("org.wso2.test.child.my-fragment");
        Assert.assertEquals(fragment, childFragment);
    }

    @Test(expectedExceptions = UUFException.class)
    public void testLookupIllegalLocalFragment() throws Exception {
        Fragment myFragment = mock(Fragment.class);
        when(myFragment.getName()).thenReturn("org.other.yourFragment");
        Lookup lookup = new Lookup(
                "org.wso2.test",
   /*bindings*/ Collections.emptyMap(),
                ImmutableSet.of(myFragment),
   /*children*/ Collections.emptySet());
        Assert.fail(lookup + " shouldn't be initialized.");// this will never run. to make ide happy
    }

}