package org.wso2.carbon.uuf;

import com.google.common.collect.ImmutableSetMultimap;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.RequestLookup;

import java.util.Collections;

import static org.mockito.Matchers.any;

public class FragmentTest {

    @Test
    public void testRenderFragment() {
        final String content = "Hello world from a fragment!";
        Renderable renderable = (model, componentLookup, requestLookup, api) -> content;
        ComponentLookup lookup = new ComponentLookup("componentName", "/componentContext", Collections.emptySet(),
                                                     Collections.emptySet(), ImmutableSetMultimap.of(),
                                                     Collections.emptySet());
        RequestLookup requestLookup = new RequestLookup("/appContext", any());
        Fragment fragment = new Fragment("fragmentName", renderable);

        String output = fragment.render(any(), lookup, requestLookup, any());
        Assert.assertEquals(output, content);
    }
}
