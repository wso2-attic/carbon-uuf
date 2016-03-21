package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableList;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.handlebars.HbsPageRenderable;

import java.util.List;
import java.util.Optional;

public class HbsPageRenderableTest {


    @Test
    public void testJs() {
        TemplateSource templateSource = new StringTemplateSource("<test>", "{{headJs \"my.js\"}} {{headJs \"ok.js\"}}");
        HbsPageRenderable renderable = new HbsPageRenderable(templateSource, Optional.empty());
        List<String> js = renderable.getHeadJs();
        Assert.assertEquals(js, ImmutableList.of("my.js", "ok.js"));
    }

}
