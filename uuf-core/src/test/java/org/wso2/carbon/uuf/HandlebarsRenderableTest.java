package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.HandlebarsError;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.handlebars.HbsRenderable;
import org.wso2.carbon.uuf.handlebars.HbsPageRenderable;
import org.wso2.carbon.uuf.core.Renderable;

import java.util.Collections;
import java.util.Map;

public class HandlebarsRenderableTest {

    @Test
    public void testZones() {
        TemplateSource source = new StringTemplateSource("my-file.hbs", "{{defineZone \"my-zone\"}}");
        HbsRenderable hb = new HbsRenderable(source);
        String output = hb.render(ImmutableMap.of("name", "Leela"),
                                  ImmutableListMultimap.of("my-zone", new MockHelloRenderable()),
                                  Collections.emptyMap());
        Assert.assertEquals(output, "Welcome to the <world> of tomorrow, Leela");
    }

    @Test
    public void testFragment() {
        TemplateSource hbsTemplate = new StringTemplateSource("my-file.hbs", "{{includeFragment \"news\"}}");
        HbsRenderable renderable = new HbsRenderable(hbsTemplate);
        Fragment fragment = new Fragment("my-news-fragment", "/mock/path", (m, b, f) -> ("Good news, " + m + "!"));
        String output = renderable.render("everyone", ImmutableListMultimap.of(), ImmutableMap.of("news", fragment));
        Assert.assertEquals(output, "Good news, everyone!");
    }

    @Test
    public void testFillingZones() {
        TemplateSource hbsTemplate = new StringTemplateSource("my-file.hbs",
                                                              "\n{{#fillZone \"my-zone\"}} {{a}}{{/fillZone}}");
        HbsPageRenderable hbsPageRenderable = new HbsPageRenderable(hbsTemplate);
        Map<String, Renderable> fillingZones = hbsPageRenderable.getFillingZones();
        Renderable fillingZone = fillingZones.get("my-zone");
        Assert.assertNotNull(fillingZone, "zone's inner content must be available under name 'my-zone'");
        try {
            fillingZone.render(Collections.emptyMap(), ImmutableListMultimap.of(), Collections.emptyMap());
            Assert.fail("can't render with an empty map since 'a' var is expected.");
        } catch (HandlebarsException ex) {
            HandlebarsError error = ex.getError();
            Assert.assertEquals(error.line, 2, "error is in the 2nd line");
            Assert.assertEquals(error.column, 26, "error is in the 26th column");
        }

        String rendered = fillingZone.render(ImmutableMap.of("a", "apple"), ImmutableListMultimap.of(),
                                             Collections.emptyMap());
        Assert.assertEquals(rendered.trim(), "apple");
    }
}
