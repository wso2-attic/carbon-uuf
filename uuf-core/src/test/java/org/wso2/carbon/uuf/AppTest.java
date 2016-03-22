package org.wso2.carbon.uuf;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
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
    public void testServeToComponent() {
        Component cmp1 = new Component("root", "", Collections.emptySet(), Collections.emptySet(),
                                       Collections.emptyMap(), Collections.emptyMap()) {
            @Override
            public Optional<String> renderPage(String pageUri) {
                return Optional.empty();
            }
        };
        Component cmp2 = new Component("test.cmp", Collections.emptySet(), Collections.emptySet(),
                                       Collections.emptyMap(), Collections.emptyMap()) {
            @Override
            public Optional<String> renderPage(String pageUri) {
                return Optional.of("rendered page from component");
            }
        };
        App app = new App("/test", ImmutableSet.of(cmp1, cmp2), ImmutableMap.of());
        String output = app.renderPage("/test/cmp/rest/of/the/url");
        Assert.assertEquals(output, "rendered page from component");
    }

}