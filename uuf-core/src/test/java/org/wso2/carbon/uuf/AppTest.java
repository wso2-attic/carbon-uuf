package org.wso2.carbon.uuf;

import com.google.common.collect.ImmutableSet;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.UriPatten;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
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
        Page testPage = new Page(testPageUriPattern, testPageLayout, testFillZones);
        List<Page> testPages = Collections.singletonList(testPage);
        Map<String, Fragment> testFragments = Collections.emptyMap();
        Map<String, Renderable> testBindings = Collections.emptyMap();
        testApp = new App(TEST_APP_CONTEXT, testPages, testFragments, testBindings, Collections.emptyMap());
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

    @Test
    public void testServeToComponent() throws Exception {
        Component cmp1 = new Component("test.cmp1", "/cmp1", Collections.emptySet(), Collections.emptySet()) {
            @Override
            public String renderPage(String pageUri) {
                return "rendered page from component 1";
            }
        };
        Component cmp2 = new Component("test.cmp2", "/cmp2", Collections.emptySet(), Collections.emptySet()) {
            @Override
            public String renderPage(String pageUri) {
                return "rendered page from component 2";
            }
        };
        Set<Component> components = ImmutableSet.of(cmp1, cmp2);
        App app = new App("/test", components);
        String output = app.renderPage(new DefaultHttpRequest(HTTP_1_0, GET, "/test/cmp1/rest/of/the/url"));
        Assert.assertEquals(output, "rendered page from component 1");
    }

}