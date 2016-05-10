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

package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.HandlebarsError;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.io.StringTemplateSource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Configuration;
import org.wso2.carbon.uuf.core.Placeholder;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.handlebars.HbsPageRenderable;
import org.wso2.carbon.uuf.handlebars.HbsRenderable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class HbsHelperTest {

    private static HbsRenderable createRenderable(String sourceStr) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsPageRenderable(stringTemplateSource);
    }

    private static ComponentLookup createLookup() {
        ComponentLookup lookup = mock(ComponentLookup.class);
        when(lookup.getConfigurations()).thenReturn(Configuration.emptyConfiguration());
        return lookup;
    }

    private static RequestLookup createRequestLookup() {
        RequestLookup requestLookup = spy(new RequestLookup("/myapp", null));
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/component/base");
        return requestLookup;
    }

    @Test
    public void testTitle() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{title \"page-title\"}}").render(null, createLookup(), requestLookup, null);
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.TITLE).get(), "page-title");
    }

    @Test
    public void testTitleWithMultipleParameters() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{title \"page\" \"-\" \"title\"}}").render(null, createLookup(), requestLookup, null);
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.TITLE).get(), "page-title");

    }

    @Test
    public void testCss() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{css \"css/my-styles.css\"}}").render(null, createLookup(), requestLookup, null);
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.CSS).get(),
                            "<link href=\"/myapp/public/component/base/css/my-styles.css\" rel=\"stylesheet\" " +
                                    "type=\"text/css\" />\n");
    }

    @Test
    public void testCssWithMultipleParameters() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{css \"css/\" \"my-styles\" \".css\"}}").render(null, createLookup(), requestLookup, null);
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.CSS).get(),
                            "<link href=\"/myapp/public/component/base/css/my-styles.css\" rel=\"stylesheet\" " +
                                    "type=\"text/css\" />\n");
    }

    @Test
    public void testHeadJs() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{headJs \"js/my-script.js\"}}").render(null, createLookup(), requestLookup, null);
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.HEAD_JS).get(),
                            "<script src=\"/myapp/public/component/base/js/my-script.js\" type=\"text/javascript\">" +
                                    "</script>\n");
    }

    @Test
    public void testHeadJsWithMultipleParameters() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{headJs \"js/\" \"my-script\" \".js\"}}").render(null, createLookup(), requestLookup, null);
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.HEAD_JS).get(),
                            "<script src=\"/myapp/public/component/base/js/my-script.js\" type=\"text/javascript\">" +
                                    "</script>\n");
    }

    @Test
    public void testHeadOther() {
        String content = "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">";
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{#headOther}}" + content + "{{/headOther}}").render(null, createLookup(), requestLookup,
                                                                               null);
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.HEAD_OTHER).get(), content);
    }

    @Test
    public void testPublic() {
        RequestLookup requestLookup = createRequestLookup();
        String template = "<img src=\"{{public \"img/my-photo.png\"}}\" />";
        String output = createRenderable(template).render(null, createLookup(), requestLookup, null);
        Assert.assertEquals(output, "<img src=\"/myapp/public/component/base/img/my-photo.png\" />");

    }

    @Test
    public void testPublicWithMultipleParameters() {
        RequestLookup requestLookup = createRequestLookup();
        String template = "<img src=\"{{public \"img/\" \"my-photo.png\"}}\" />";
        String output = createRenderable(template).render(null, createLookup(), requestLookup, null);
        Assert.assertEquals(output, "<img src=\"/myapp/public/component/base/img/my-photo.png\" />");

    }

    @Test
    public void testJs() {
        RequestLookup requestLookup = createRequestLookup();
        createRenderable("{{js \"js/my-other-script.js\"}}").render(null, createLookup(), requestLookup, null);
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.BODY_JS).get(),
                            "<script src=\"/myapp/public/component/base/js/my-other-script.js\" " +
                                    "type=\"text/javascript\"></script>\n");
    }

    @Test
    public void testMissing() {
        RequestLookup requestLookup = createRequestLookup();
        HbsRenderable renderable = createRenderable("foo\nbar\n{{abc param1=\"p1\"}}\nfoobar");
        try {
            renderable.render(null, createLookup(), requestLookup, null);
            Assert.fail("Variable or helper named 'abc' does not exists.");
        } catch (HandlebarsException ex) {
            HandlebarsError error = ex.getError();
            Assert.assertEquals(error.line, 3, "error is in the 3rd line");
            Assert.assertEquals(error.column, 2, "error is in the 2nd column");
        }

    }
}
