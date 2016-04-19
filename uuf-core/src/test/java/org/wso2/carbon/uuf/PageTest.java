package org.wso2.carbon.uuf;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;

import static org.mockito.Matchers.any;

public class PageTest {

    @Test
    public void testRenderPage() throws Exception {
        Renderable renderable = (model, componentLookup, requestLookup, api) -> "Hello world!";
        Page page = new Page(any(), renderable);
        String output = page.render(any(), any(), any(), any());
        Assert.assertEquals(output, "Hello world!");
    }

}
