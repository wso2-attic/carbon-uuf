package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.HandlebarsError;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.HandlebarsRenderable;
import org.wso2.carbon.uuf.core.Renderable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class HandlebarsRenderableTest {

    @Test
    public void testZones() {

        StringTemplateSource source = new StringTemplateSource("my-file.hbs", "{{defineZone \"my-zone\"}}");
        HandlebarsRenderable hb = new HandlebarsRenderable(source);
        String s = hb.render(
                ImmutableMap.of("name", "Leela"),
                ImmutableMap.of("my-zone", new MockHelloRenderable()),
                Collections.emptyMap());
        Assert.assertEquals(s, "Welcome to the <world> of tomorrow, Leela");
    }

    @Test
    public void testLayoutName() {
        HandlebarsRenderable renderble = new HandlebarsRenderable(new StringTemplateSource(
                "my-file.hbs",
                "{{layout \"my-layout\"}}"));

        String layoutName = renderble.getLayoutName();
        Assert.assertEquals(layoutName, "my-layout", "a layout is defined in the template");

    }

    @Test
    public void testFragment() {
        HandlebarsRenderable renderble = new HandlebarsRenderable(new StringTemplateSource(
                "my-file.hbs",
                "{{includeFragment \"news\"}}"));
        Fragment fragment = new Fragment(
                "my-news-fragment",
                (o, z, f) -> "Good news, " + o + "!",
                /*script*/ Optional.empty());
        String news = renderble.render(
                "everyone",
                Collections.emptyMap(),
                ImmutableMap.of("news", fragment));
        Assert.assertEquals(news, "Good news, everyone!");
    }

    @Test
    public void testFillingZones() {
        HandlebarsRenderable renderble = new HandlebarsRenderable(new StringTemplateSource(
                "my-file.hbs",
                "\n{{#fillZone \"my-zone\"}} {{a}}{{/fillZone}}"));
        Map<String, Renderable> fillingZones = renderble.getFillingZones();
        Renderable fillingZone = fillingZones.get("my-zone");
        Assert.assertNotNull(fillingZone, "zone's inner content must be available under name 'my-zone'");
        try {
            fillingZone.render(
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap());
            Assert.fail("can't render with an empty map since 'a' var is expected.");
        } catch (HandlebarsException ex) {
            HandlebarsError error = ex.getError();
            Assert.assertEquals(error.line, 2, "error is in the 2nd line");
            Assert.assertEquals(error.column, 26, "error is in the 26th column");
        }

        String rendered = fillingZone.render(
                ImmutableMap.of("a", "apple"),
                Collections.emptyMap(),
                Collections.emptyMap());
        Assert.assertEquals(rendered.trim(), "apple");
    }
}
