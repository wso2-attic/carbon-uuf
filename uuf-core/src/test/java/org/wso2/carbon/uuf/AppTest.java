package org.wso2.carbon.uuf;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.*;
import org.wso2.carbon.uuf.handlebars.Executable;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class AppTest {
    private static final String TEST_APP_CONTEXT = "/test-app";
    private static final String TEST_PAGE_URI = "/page1";
    private static final String TEST_PAGE_CONTENT = "Hello World!!!";
    private App testApp;

    @BeforeClass
    public void init() {
        UriPatten testPageUriPattern = new UriPatten(TEST_PAGE_URI);
        Renderable testPageLayout = (model, bindings, fragments) -> TEST_PAGE_CONTENT;
        Map<String, Renderable> testFillZones = Collections.emptyMap();
        Page testPage = new Page(testPageUriPattern, testPageLayout, testFillZones, Optional.<Executable>empty());
        List<Page> testPages = Collections.singletonList(testPage);
        List<Fragment> testFragments = Collections.emptyList();
        Map<String, Renderable> testBindings = Collections.emptyMap();
        testApp = new App(TEST_APP_CONTEXT, testPages, testFragments, testBindings);
    }

    @Test
    public void testMatchingPage() throws Exception {
        String output = testApp.renderPage(new DefaultFullHttpRequest(HTTP_1_1, GET, TEST_APP_CONTEXT + TEST_PAGE_URI));
        Assert.assertEquals(output, TEST_PAGE_CONTENT, "Since url matches, should render the page content.");
    }

    @Test
    public void testMissingPage() throws Exception {
        try {
            testApp.renderPage(new DefaultFullHttpRequest(HTTP_1_1, GET, TEST_APP_CONTEXT + "/page2"));
            Assert.fail("An exception should be thrown because the page2 doesn't exist");
        } catch (UUFException e) {
            Assert.assertEquals(e.getStatus(), Response.Status.NOT_FOUND, "page doesn't exist.");
        }
    }


}