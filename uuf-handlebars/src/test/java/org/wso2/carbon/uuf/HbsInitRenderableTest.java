package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.HandlebarsError;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.io.StringTemplateSource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.Model;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.handlebars.HbsInitRenderable;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;

public class HbsInitRenderableTest {
    private static HbsInitRenderable createHbsRenderable(String sourceStr) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsInitRenderable(stringTemplateSource, Optional.empty());
    }

    @Test
    public void testNoZonesInZones() {
        String content = "\n{{#fillZone \"myZone\"}}\n{{#fillZone \"innerZone\"}}{{/fillZone}}{{/fillZone}}";
        try {
            HbsInitRenderable renderable = createHbsRenderable(content);
            Map<String, HbsInitRenderable> fillingZones = renderable.getFillingZones();
            Assert.assertEquals(fillingZones.size(), 1); //should never hit, to create rid of unused warning
            Assert.fail("fill zone inside fill zone is not valid.");
        } catch (HandlebarsException e) {
            Assert.assertTrue(e.getMessage().contains("not valid"));
        }
    }

    @Test
    public void testFillingZonesLineNumbers() {
        HbsInitRenderable renderable = createHbsRenderable("\nfilling\n*{{#fillZone \"my-zone\"}} {{a}}{{/fillZone}}");
        Map<String, HbsInitRenderable> fillingZones = renderable.getFillingZones();
        Renderable fillingZone = fillingZones.get("my-zone");
        Assert.assertNotNull(fillingZone, "zone's inner content must be available under name 'my-zone'");
        try {
            fillingZone.render("/url", mock(Model.class), mock(Lookup.class));
            Assert.fail("can't render with an empty map since 'a' var is expected.");
        } catch (HandlebarsException ex) {
            HandlebarsError error = ex.getError();
            Assert.assertEquals(error.line, 3, "error is in the 3nd line");
            Assert.assertEquals(error.column, 27, "error is in the 26th column");
        }
    }
}