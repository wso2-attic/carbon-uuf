package org.wso2.carbon.uuf;

import com.google.common.collect.ImmutableMap;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.Executable;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UriPatten;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class PageTest {
    private final static Map<String, Renderable> FRAGMENTS = Collections.emptyMap();

    @Test
    public void testRenderNonExecPage() throws Exception {
        Page page = new Page(new UriPatten("/page1"), new MockHelloRenderable());
        String pageOutput = page.serve(new DefaultFullHttpRequest(HTTP_1_1, GET, "/my-app/page1"), FRAGMENTS);
        Assert.assertEquals(pageOutput,
                "Welcome to the <world> of tomorrow !", "page should render with an empty model");
    }

    @Test
    public void testRenderPage() throws Exception {
        Renderable template = new MockHelloRenderable();
        Optional<Executable> executable = Optional.of(new MockExecutable());
        Page page = new Page(new UriPatten("/page1"), template, executable, Optional.empty());
        String pageOutput = page.serve(new DefaultFullHttpRequest(HTTP_1_1, GET, "/my-app/page1"), FRAGMENTS);
        Assert.assertEquals(pageOutput, "Welcome to the <world> of tomorrow, Fry", "page should render with a model");
    }

    @Test
    public void testPageWithLayout() throws Exception {
        Optional<Executable> executable = Optional.of(() -> ImmutableMap.of("name", "Bender"));
        Optional<Renderable> renderable = Optional.of(
                (o, zones, fragments) -> zones.get("all-zone").render(o, zones, fragments));

        Page page = new Page(
                new UriPatten("/page1"),
                new Renderable() {
                    @Override
                    public String render(Object o, Map<String, Renderable> zones, Map<String, Renderable> fragments) {
                        return null;
                    }

                    @Override
                    public Map<String, Renderable> getFillingZones() {
                        return ImmutableMap.of("all-zone", new MockHelloRenderable());
                    }
                },
                executable,
                renderable
        );
        String pageOutput = page.serve(new DefaultFullHttpRequest(HTTP_1_1, GET, "/my-app/page1"), FRAGMENTS);
        Assert.assertEquals(pageOutput, "Welcome to the <world> of tomorrow, Bender", "page should render with a model");
    }
}
