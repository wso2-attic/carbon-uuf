package org.wso2.carbon.uuf;

import com.google.common.collect.ImmutableSet;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.Component;

import java.util.Collections;
import java.util.Optional;

public class AppTest {
    private static final String TEST_APP_CONTEXT = "/test-app";
    private static final String TEST_PAGE_URI = "/page1";
    private static final String TEST_PAGE_CONTENT = "Hello World!!!";
    private App testApp;

//    @BeforeClass
//    public void init() {
//        UriPatten testPageUriPattern = new UriPatten(TEST_PAGE_URI);
//        Renderable testPageLayout = (model, bindings, fragments) -> TEST_PAGE_CONTENT;
//        Map<String, Renderable> testFillZones = Collections.emptyMap();
//        Page testPage = new Page(testPageUriPattern, testPageLayout, testFillZones);
//        List<Page> testPages = Collections.singletonList(testPage);
//        Map<String, Fragment> testFragments = Collections.emptyMap();
//        Map<String, Renderable> testBindings = Collections.emptyMap();
//        testApp = new App(TEST_APP_CONTEXT, testPages, testFragments, testBindings, Collections.emptyMap());
//    }
//
//    @Test
//    public void testMatchingPage() throws Exception {
//        String output = testApp.renderPage(TEST_APP_CONTEXT + TEST_PAGE_URI);
//        Assert.assertEquals(output, TEST_PAGE_CONTENT, "Since url matches, should render the page content.");
//    }
//
//    @Test
//    public void testMissingPage() throws Exception {
//        try {
//            testApp.renderPage(TEST_APP_CONTEXT + "/page2");
//            Assert.fail("An exception should be thrown because the page2 doesn't exist");
//        } catch (UUFException e) {
//            Assert.assertEquals(e.getStatus(), Response.Status.NOT_FOUND, "page doesn't exist.");
//        }
//    }

    //TODO: enable
    @Test
    public void testServeToComponent() {
//
//        Component cmp1 = new Component(
//                "root",
//                "/",
//                "1.0.0",
//               /*pages*/ Collections.emptySortedSet(),
//               /*fragments*/ Collections.emptySet(),
//               /*config*/ Collections.emptyMap(),
//               /*binding*/ Collections.emptyMap()) {
//            @Override
//            public Optional<String> renderPage(String pageUri) {
//                return Optional.empty();
//            }
//        };
//        Component cmp2 = new Component(
//                "test.component.orange",
//                "/orange",
//                "1.0.0",
//               /*pages*/ Collections.emptySortedSet(),
//               /*fragments*/ Collections.emptySet(),
//               /*config*/ Collections.emptyMap(),
//               /*binding*/ Collections.emptyMap()) {
//            @Override
//            public Optional<String> renderPage(String pageUri) {
//                return Optional.of("page " + pageUri + " rendered");
//            }
//        };
//        App app = new App("/test", ImmutableSet.of(cmp1, cmp2));
//        String output = app.renderPage("/orange/rest/of/the/url");
//        Assert.assertEquals(output, "page /rest/of/the/url rendered");
    }

}