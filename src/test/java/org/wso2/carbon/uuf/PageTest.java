package org.wso2.carbon.uuf;

import com.google.common.collect.ImmutableMap;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderble;
import org.wso2.carbon.uuf.core.UriPatten;

import java.util.Collections;
import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class PageTest {
    private final static Map<String, Fragment> FRAGMENTS = Collections.emptyMap();

    @Test
    public void testRenderNonExecPage() throws Exception {
        Page page = new Page(new UriPatten("/page1"), new MockHelloRenderble());
        String pageOutput = page.serve(new DefaultFullHttpRequest(HTTP_1_1, GET, "/my-app/page1"), FRAGMENTS);
        Assert.assertEquals(pageOutput,
                "Welcome to the <world> of tomorrow !", "page should render with an empty model");
    }

    @Test
    public void testRenderPage() throws Exception {
        Page page = new Page(new UriPatten("/page1"), new MockHelloRenderble(), new MockExecutable(), null);
        String pageOutput = page.serve(new DefaultFullHttpRequest(HTTP_1_1, GET, "/my-app/page1"), FRAGMENTS);
        Assert.assertEquals(pageOutput, "Welcome to the <world> of tomorrow, Fry", "page should render with a model");
    }

    @Test
    public void testPageWithLayout() throws Exception {
        Page page = new Page(
                new UriPatten("/page1"),
                new Renderble() {
                    @Override
                    public String render(Object o, Map<String, Renderble> zones) {
                        return null;
                    }

                    @Override
                    public Map<String, Renderble> getFillingZones() {
                        return ImmutableMap.of("all-zone", new MockHelloRenderble());
                    }
                },
                () -> ImmutableMap.of("name", "Bender"),
                (data, zones) -> zones.get("all-zone").render(data, Collections.emptyMap())
        );
        String pageOutput = page.serve(new DefaultFullHttpRequest(HTTP_1_1, GET, "/my-app/page1"), FRAGMENTS);
        Assert.assertEquals(pageOutput, "Welcome to the <world> of tomorrow, Bender", "page should render with a model");
    }
}
