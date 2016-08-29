/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.HandlebarsError;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.io.StringTemplateSource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.Configuration;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.HbsPageRenderable;
import org.wso2.carbon.uuf.spi.HttpRequest;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class HbsHelperTest {

    private static HbsRenderable createRenderable(String sourceStr) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsPageRenderable(stringTemplateSource);
    }

    private static Lookup createLookup() {
        Lookup lookup = mock(Lookup.class);
        when(lookup.getConfiguration()).thenReturn(new Configuration());
        return lookup;
    }

    private static RequestLookup createRequestLookup() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getQueryParams()).thenReturn(Collections.emptyMap());
        return spy(new RequestLookup("/contextPath", request, null));
    }

    private static API createAPI() {
        API api = mock(API.class);
        when(api.getSession()).thenReturn(Optional.empty());
        return api;
    }

    @Test
    public void testFavicon() {
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/component/base");
        createRenderable("{{favicon \"img/favicon.png\" type=\"image/png\"}}").render(null, createLookup(),
                                                                                      requestLookup, createAPI());
        String expected = "<link rel=\"shortcut icon\" href=\"/myapp/public/component/base/img/favicon.png\" " +
                "type=\"image/png\" />\n";

        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.favicon).get(), expected);
        String output = createRenderable("X {{placeholder \"favicon\"}} Y").render(null, createLookup(), requestLookup,
                                                                                   createAPI());
        Assert.assertEquals(output, "X " + expected + " Y");
    }

    @Test
    public void testFaviconWithMultipleParameters() {
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/component/base");
        createRenderable("{{favicon \"img/\" \"favicon\" \".png\"}}").render(null, createLookup(), requestLookup,
                                                                             createAPI());
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.favicon).get(),
                            "<link rel=\"shortcut icon\" href=\"/myapp/public/component/base/img/favicon.png\" />\n");

    }

    @Test
    public void testTitle() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{title \"page-title\"}}").render(null, createLookup(), requestLookup, createAPI());
        String expected = "page-title";

        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.title).get(), expected);
        String output = createRenderable("X {{placeholder \"title\"}} Y").render(null, createLookup(), requestLookup,
                                                                                 createAPI());
        Assert.assertEquals(output, "X " + expected + " Y");
    }

    @Test
    public void testTitleWithMultipleParameters() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{title \"page\" \"-\" \"title\"}}").render(null, createLookup(), requestLookup, createAPI());
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.title).get(), "page-title");

    }

    @Test
    public void testCss() {
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/component/base");
        createRenderable("{{css \"css/my-styles.css\"}}").render(null, createLookup(), requestLookup, createAPI());
        String expected = "<link href=\"/myapp/public/component/base/css/my-styles.css\" rel=\"stylesheet\" " +
                "type=\"text/css\" />\n";

        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.css).get(), expected);
        String output = createRenderable("X {{placeholder \"css\"}} Y").render(null, createLookup(), requestLookup,
                                                                               createAPI());
        Assert.assertEquals(output, "X " + expected + " Y");
    }

    @Test
    public void testCssWithMultipleParameters() {
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/component/base");
        createRenderable("{{css \"css/\" \"my-styles\" \".css\"}}").render(null, createLookup(), requestLookup,
                                                                           createAPI());
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.css).get(),
                            "<link href=\"/myapp/public/component/base/css/my-styles.css\" rel=\"stylesheet\" " +
                                    "type=\"text/css\" />\n");
    }

    @Test
    public void testHeadJs() {
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/component/base");
        createRenderable("{{headJs \"js/my-script.js\"}}").render(null, createLookup(), requestLookup, createAPI());
        String expected = "<script src=\"/myapp/public/component/base/js/my-script.js\" type=\"text/javascript\">" +
                "</script>\n";

        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.headJs).get(), expected);
        String output = createRenderable("X {{placeholder \"headJs\"}} Y").render(null, createLookup(), requestLookup,
                                                                                  createAPI());
        Assert.assertEquals(output, "X " + expected + " Y");
    }

    @Test
    public void testHeadJsWithMultipleParameters() {
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/component/base");
        createRenderable("{{headJs \"js/\" \"my-script\" \".js\"}}").render(null, createLookup(), requestLookup,
                                                                            createAPI());
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.headJs).get(),
                            "<script src=\"/myapp/public/component/base/js/my-script.js\" type=\"text/javascript\">" +
                                    "</script>\n");
    }

    @Test
    public void testJs() {
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/component/base");
        createRenderable("{{js \"js/bottom-script.js\"}}").render(null, createLookup(), requestLookup, createAPI());
        String expected = "<script src=\"/myapp/public/component/base/js/bottom-script.js\" type=\"text/javascript\">" +
                "</script>\n";

        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.js).get(), expected);
        String output = createRenderable("X {{placeholder \"js\"}} Y").render(null, createLookup(), requestLookup,
                                                                              createAPI());
        Assert.assertEquals(output, "X " + expected + " Y");
    }

    @Test
    public void testHeadOther() {
        String content = "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">";
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{#headOther}}" + content + "{{/headOther}}").render(null, createLookup(), requestLookup,
                                                                               createAPI());

        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.headOther).get(), content);
        String output = createRenderable("X {{placeholder \"headOther\"}} Y").render(null, createLookup(),
                                                                                     requestLookup, createAPI());
        Assert.assertEquals(output, "X " + content + " Y");
    }

    @Test
    public void testPublic() {
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/component/base");
        String template = "<img src=\"{{public \"img/my-photo.png\"}}\" />";
        String output = createRenderable(template).render(null, createLookup(), requestLookup, createAPI());
        Assert.assertEquals(output, "<img src=\"/myapp/public/component/base/img/my-photo.png\" />");

    }

    @Test
    public void testPublicWithMultipleParameters() {
        RequestLookup requestLookup = createRequestLookup();
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/component/base");
        String template = "<img src=\"{{public \"img/\" \"my-photo.png\"}}\" />";
        String output = createRenderable(template).render(null, createLookup(), requestLookup, createAPI());
        Assert.assertEquals(output, "<img src=\"/myapp/public/component/base/img/my-photo.png\" />");

    }

    @Test
    public void testSecured() {
        String content = "{{#secured}} secured content {{else}} un-secured content {{/secured}}";
        String output = createRenderable(content).render(null, createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, " un-secured content ");
    }

    @Test
    public void testMissing() {
        RequestLookup requestLookup = createRequestLookup();
        HbsRenderable renderable = createRenderable("foo\nbar\n{{abc param1=\"p1\"}}\nfoobar");
        try {
            renderable.render(null, createLookup(), requestLookup, createAPI());
            Assert.fail("Variable or helper named 'abc' does not exists.");
        } catch (HandlebarsException ex) {
            HandlebarsError error = ex.getError();
            Assert.assertEquals(error.line, 3, "error is in the 3rd line");
            Assert.assertEquals(error.column, 2, "error is in the 2nd column");
        }

    }
}
