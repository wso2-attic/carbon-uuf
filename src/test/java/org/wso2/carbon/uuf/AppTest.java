package org.wso2.carbon.uuf;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderble;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.UriPatten;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class AppTest {
    private static final String MOCK_PAGE1_CONTENT = "mock page1 content";
    private final static List<Page> PAGES =
            Collections.singletonList(new Page(new UriPatten("/page1"), null, null, null) {
                @Override
                public String serve(HttpRequest request, Map<String, Renderble> fragments) {
                    return MOCK_PAGE1_CONTENT;
                }
            });
    private final static List<Fragment> FRAGMENTS = Collections.emptyList();
    private final static App APP = new App("/my-APP", PAGES, FRAGMENTS);

    @Test
    public void testMatchingPage() throws Exception {
        String appReturn = APP.serve(new DefaultFullHttpRequest(HTTP_1_1, GET, "/my-APP/page1"));
        Assert.assertEquals(appReturn, MOCK_PAGE1_CONTENT, "Since url matches, should serve the page content.");
    }

    @Test
    public void testMissingPage() throws Exception {
        try {
            APP.serve(new DefaultFullHttpRequest(HTTP_1_1, GET, "/my-APP/page2"));
            Assert.fail("an exception should be thrown because the page2 doesn't exist");
        } catch (UUFException e) {
            Assert.assertEquals(e.getStatus(), Response.Status.NOT_FOUND, "page doesn't exist.");
        }
    }


}