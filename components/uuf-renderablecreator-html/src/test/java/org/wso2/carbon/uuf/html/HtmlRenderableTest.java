package org.wso2.carbon.uuf.html;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.renderablecreator.html.core.HtmlRenderable;

public class HtmlRenderableTest {

    @Test
    public void testHtmlContent() {
        final String htmlContent = "<html>\n" +
                "<head>\n" +
                "<title>Sample HTML File</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<p>This is a sample HTML file.</p>\n" +
                "</body>\n" +
                "</html>";

        HtmlRenderable htmlRenderable = new HtmlRenderable(htmlContent);
        String output = htmlRenderable.render(null, null, null, null);
        Assert.assertEquals(output, htmlContent);
    }
}
