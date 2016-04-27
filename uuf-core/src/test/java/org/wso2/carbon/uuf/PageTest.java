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
                                                     Collections.emptySet());
        RequestLookup requestLookup = new RequestLookup("/appContext", any());
        Page page = new Page(any(), renderable);

        String output = page.render(any(), lookup, requestLookup, any());
        Assert.assertEquals(output, content);
    }
}
