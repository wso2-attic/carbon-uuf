package org.wso2.carbon.uuf;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.model.Model;

import static org.mockito.Mockito.mock;

public class PageTest {

    @Test
    public void testRenderPage() throws Exception {
        Renderable renderable = (uri, model, lookup) -> "Hello world!";
        Page page = new Page(mock(UriPatten.class), renderable, mock(Lookup.class));
        String output = page.serve("/url", mock(Model.class), mock(Lookup.class));
        Assert.assertEquals(output, "Hello world!");
    }

}
