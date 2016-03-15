package org.wso2.carbon.uuf;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.handlebars.Executable;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UriPatten;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class PageTest {
    private static final String TEST_PAGE_URI = "/page1";
    private static final String TEST_PAGE_CONTENT = "Hello World!!!";

    @Test
    public void testRenderPage() throws Exception {
        UriPatten testPageUriPattern = new UriPatten(TEST_PAGE_URI);
        Renderable testPageLayout = (model, bindings, fragments) -> TEST_PAGE_CONTENT;
        Map<String, Renderable> testFillZones = Collections.emptyMap();
        Page testPage = new Page(testPageUriPattern, testPageLayout, testFillZones, Optional.<Executable>empty());
        Map<String, Renderable> testBindings = Collections.emptyMap();
        Map<String, Fragment> testFragments = Collections.emptyMap();

        String output = testPage.serve(null, testBindings, testFragments);
        Assert.assertEquals(output, TEST_PAGE_CONTENT, "Page renders without a model");
    }

    @Test
    public void testRenderPageWithModel() throws Exception {
        UriPatten testPageUriPattern = new UriPatten(TEST_PAGE_URI);
        Renderable testPageLayout = (model, bindings, fragments) -> TEST_PAGE_CONTENT;
        Map<String, Renderable> testFillZones = Collections.emptyMap();
        Optional<Executable> testExecutable = Optional.of(new MockExecutable());
        Page testPage = new Page(testPageUriPattern, testPageLayout, testFillZones, testExecutable);
        Map<String, Renderable> testBindings = Collections.emptyMap();
        Map<String, Fragment> testFragments = Collections.emptyMap();
        //
        String output = testPage.serve(null, testBindings, testFragments);
        Assert.assertEquals(output, TEST_PAGE_CONTENT, "Page renders with a model");
    }
}
