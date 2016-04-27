package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.HandlebarsError;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.io.StringTemplateSource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.handlebars.HbsPagePreprocessor;
import org.wso2.carbon.uuf.model.MapModel;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class HbsPagePreprocessorTest {

    private static HbsPagePreprocessor createHbsRenderable(String sourceStr) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsPagePreprocessor(stringTemplateSource, Optional.empty());
    }

    @Test
    public void testNoZonesInZones() {
        String content = "\n{{#fillZone \"myZone\"}}\n{{#fillZone \"innerZone\"}}{{/fillZone}}{{/fillZone}}";
        try {
            HbsPagePreprocessor renderable = createHbsRenderable(content);
            Map<String, HbsPagePreprocessor> fillingZones = renderable.getFillingZones();
            Assert.assertEquals(fillingZones.size(), 1); //should never hit, to create rid of unused warning
            Assert.fail("fill zone inside fill zone is not valid.");
        } catch (HandlebarsException ex) {
            HandlebarsError error = ex.getError();
            Assert.assertEquals(error.line, 2, "error is in the 2nd line");
            Assert.assertEquals(error.column, 3, "error is in the 3rd column");
        }
    }

    @Test
    public void testFillingZonesLineNumbers() {
        HbsPagePreprocessor renderable = createHbsRenderable("\nfilling\n*{{#fillZone \"my-zone\"}} {{a}}{{/fillZone}}");
        Map<String, HbsPagePreprocessor> fillingZones = renderable.getFillingZones();
        Renderable fillingZone = fillingZones.get("my-zone");
        Assert.assertNotNull(fillingZone, "zone's inner content must be available under name 'my-zone'");
        try {
            fillingZone.render(new MapModel(Collections.emptyMap()), null, null, null);
            Assert.fail("can't render with an empty map since 'a' var is expected.");
        } catch (HandlebarsException ex) {
            HandlebarsError error = ex.getError();
            Assert.assertEquals(error.line, 3, "error is in the 3rd line");
            Assert.assertEquals(error.column, 27, "error is in the 27th column");
        }
    }
}