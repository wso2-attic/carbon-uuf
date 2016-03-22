package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableList;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.handlebars.HbsInitRenderable;

import java.util.List;
import java.util.Optional;

public class HbsInitRenderableTest {

    @Test
    public void testHeadJsInZones() {
        String content = "{{#fillZone \"myZone\"}}{{headJs \"my.js\"}}{{headJs \"ok.js\"}}{{/fillZone}}";
        TemplateSource templateSource = new StringTemplateSource("<test>", content);
        HbsInitRenderable renderable = new HbsInitRenderable(templateSource, Optional.empty());
        List<String> js = renderable.getFillingZones().get("myZone").getHeadJs();
        Assert.assertEquals(js, ImmutableList.of("my.js", "ok.js"));
    }

    @Test
    public void testHeadJs() {
        TemplateSource templateSource = new StringTemplateSource("<test>", "{{headJs \"my.js\"}} {{headJs \"ok.js\"}}");
        HbsInitRenderable renderable = new HbsInitRenderable(templateSource, Optional.empty());
        List<String> js = renderable.getHeadJs();
        Assert.assertEquals(js, ImmutableList.of("my.js", "ok.js"));
    }

}
