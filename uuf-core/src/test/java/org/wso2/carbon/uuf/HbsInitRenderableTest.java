package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.HandlebarsError;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.handlebars.HbsInitRenderable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            Assert.assertEquals(fillingZones.size(), 1); //should never hit, to get rid of unused warning
            Assert.fail("fill zone inside fill zone is not valid.");
        } catch (HandlebarsException e) {
            Assert.assertTrue(e.getMessage().contains("not valid"));
        }
    }

    @Test
    public void testHeadJsInZones() {
        String content = "{{#fillZone \"myZone\"}}{{headerJs \"/my.js\"}}{{headerJs \"/ok.js\"}}{{/fillZone}}";
        TemplateSource templateSource = new StringTemplateSource("<test>", content);
        HbsInitRenderable renderable = new HbsInitRenderable(templateSource, Optional.empty());
        List<String> js = renderable.getFillingZones().get("myZone").getHeadJs();
        Assert.assertEquals(js, ImmutableList.of("/my.js", "/ok.js"));
    }

    @Test
    public void testHeaderJs() {
        TemplateSource templateSource = new StringTemplateSource("<test>", "{{headerJs \"/my.js\"}} {{headerJs \"/ok.js\"}}");
        HbsInitRenderable renderable = new HbsInitRenderable(templateSource, Optional.empty());
        List<String> js = renderable.getHeadJs();
        Assert.assertEquals(js, ImmutableList.of("/my.js", "/ok.js"));
    }

    @Test
    public void testFillingZonesLineNumbers() {
        HbsInitRenderable renderable = createHbsRenderable("\nfilling\n*{{#fillZone \"my-zone\"}} {{a}}{{/fillZone}}");
        Map<String, HbsInitRenderable> fillingZones = renderable.getFillingZones();
        Renderable fillingZone = fillingZones.get("my-zone");
        Assert.assertNotNull(fillingZone, "zone's inner content must be available under name 'my-zone'");
        try {
            fillingZone.render(new Object(), ImmutableMultimap.of(), Collections.emptyMap());
            Assert.fail("can't render with an empty map since 'a' var is expected.");
        } catch (HandlebarsException ex) {
            HandlebarsError error = ex.getError();
            Assert.assertEquals(error.line, 3, "error is in the 3nd line");
            Assert.assertEquals(error.column, 27, "error is in the 26th column");
        }
    }

}
