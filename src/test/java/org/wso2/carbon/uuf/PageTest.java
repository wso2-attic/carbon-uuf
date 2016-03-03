package org.wso2.carbon.uuf;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.UriPatten;

import java.util.Collections;
import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class PageTest {
    private final static Map<String, Fragment> FRAGMENTS = Collections.emptyMap();

    @Test
    public void testRenderNonExecPage() throws Exception {
        Page page = new Page(new UriPatten("/page1"), new MockRenderble());
        String pageOutput = page.serve(new DefaultFullHttpRequest(HTTP_1_1, GET, "/my-app/page1"), FRAGMENTS);
        Assert.assertEquals(pageOutput, "Welcome to the world of tomorrow !", "page should render with an empty model");
    }

    @Test
    public void testRenderPage() throws Exception {
        Page page = new Page(new UriPatten("/page1"), new MockRenderble(), new MockExecutable());
        String pageOutput = page.serve(new DefaultFullHttpRequest(HTTP_1_1, GET, "/my-app/page1"), FRAGMENTS);
        Assert.assertEquals(pageOutput, "Welcome to the world of tomorrow, Fry", "page should render with a model");
    }
}
