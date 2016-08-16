package org.wso2.carbon.uuf.html;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.Configuration;
import org.wso2.carbon.uuf.api.model.MapModel;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.html.core.HtmlRenderable;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.model.Model;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class HtmlRenderableTest {

    private static HtmlRenderable createHtmlRenderable(String htmlContent) {
        return new HtmlRenderable(htmlContent);
    }

    private static Model createModel() {
        return new MapModel(Collections.emptyMap());
    }

    private static Lookup createLookup() {
        Lookup lookup = mock(Lookup.class);
        when(lookup.getConfiguration()).thenReturn(Configuration.emptyConfiguration());
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
    public void testHtmlContent() {
        final String htmlContent = "<html>\n" +
                "<head>\n" +
                "<title>Sample HTML File</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<p>This is a sample HTML file.</p>\n" +
                "</body>\n" +
                "</html>";

        HtmlRenderable htmlRenderable = createHtmlRenderable(htmlContent);
        String output = htmlRenderable.render(createModel(), createLookup(), createRequestLookup(), createAPI());
        Assert.assertEquals(output, htmlContent);
    }
}
