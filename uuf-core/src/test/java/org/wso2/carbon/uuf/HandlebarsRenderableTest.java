package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.handlebars.Executable;
import org.wso2.carbon.uuf.handlebars.HbsRenderable;
import org.wso2.carbon.uuf.handlebars.JSExecutable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class HandlebarsRenderableTest {
    private static HbsRenderable createHbsRenderable(String sourceStr) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsRenderable(stringTemplateSource, Optional.empty());
    }

    private static HbsRenderable createHbsRenderable(String sourceStr, Executable executable) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsRenderable(stringTemplateSource, Optional.of(executable));
    }

    @Test
    public void testTemplate() {
        final String templateContent = "A Plain Handlebars template.";
        HbsRenderable hbsRenderable = createHbsRenderable(templateContent);
        String output = hbsRenderable.render(new Object(), ImmutableListMultimap.of(), Collections.emptyMap());
        Assert.assertEquals(output, templateContent);
    }

    @Test
    public void testTemplateWithModel() {
        HbsRenderable hbsRenderable = createHbsRenderable("Hello {{name}}! Have a good day.");
        Map model = ImmutableMap.of("name", "Alice");
        String output = hbsRenderable.render(model, ImmutableListMultimap.of(), Collections.emptyMap());
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testTemplateWithExecutable() {
        Executable executable = context -> ImmutableMap.of("name", "Alice");
        HbsRenderable hbsRenderable = createHbsRenderable("Hello {{name}}! Have a good day.", executable);
        String output = hbsRenderable.render(new Object(), ImmutableListMultimap.of(), Collections.emptyMap());
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testTemplateWithJsExecutable() {
        JSExecutable script = new JSExecutable("function onRequest(){ return {name: \"Alice\"}; }", Optional.empty(), HandlebarsRenderableTest.class.getClassLoader());
        HbsRenderable hbsRenderable = createHbsRenderable("Hello {{name}}! Have a good day.", script);
        String output = hbsRenderable.render(new Object(), ImmutableListMultimap.of(), Collections.emptyMap());
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testFragment() {
        HbsRenderable hbsRenderable = createHbsRenderable("{{includeFragment \"test-fragment\"}}");
        final String fragmentContent = "This is the content of the test-fragment.";
        HbsRenderable fragmentRenderable = createHbsRenderable(fragmentContent);
        Fragment fragment = new Fragment("test-fragment", fragmentRenderable);
        String output = hbsRenderable.render(new Object(), ImmutableListMultimap.of(), ImmutableMap.of("test-fragment",
                                                                                                       fragment));
        Assert.assertEquals(output, fragmentContent);
    }

    @Test
    public void testFragmentWithDefineZone() {
        final String fragmentContent = "This is the content of the test-fragment.";
        HbsRenderable fragmentRenderable = createHbsRenderable(fragmentContent + "{{defineZone \"test-zone\"}}");
        Fragment fragment = new Fragment("test-fragment", fragmentRenderable);

        final String zoneContent = "This is the content of the test-zone.";
        HbsRenderable fillZoneRenderable = createHbsRenderable(zoneContent);

        HbsRenderable hbsRenderable = createHbsRenderable("{{includeFragment \"test-fragment\"}}");
        Multimap<String, Renderable> bindings = ImmutableListMultimap.of("test-zone", fillZoneRenderable);
        String output = hbsRenderable.render(new Object(), bindings, ImmutableMap.of("test-fragment", fragment));
        Assert.assertEquals(output, (fragmentContent + zoneContent));
    }

    @Test
    public void testZones() {
        HbsRenderable defineZoneRenderable = createHbsRenderable("{{defineZone \"test-zone\"}}");
        final String zoneContent = "This is the content of the test-zone.";
        HbsRenderable fillZoneRenderable = createHbsRenderable(zoneContent);
        Multimap<String, Renderable> bindings = ImmutableListMultimap.of("test-zone", fillZoneRenderable);
        String output = defineZoneRenderable.render(new Object(), bindings, Collections.emptyMap());
        Assert.assertEquals(output, zoneContent);
    }

    @Test
    public void testHeaderJs() {
        HbsRenderable hbsRenderable = createHbsRenderable("<head>{{placeholder \"headerJs\"}}</head>" +
                                                                  "{{headerJs \"/my.js\"}}{{headerJs \"/ok.js\"}}");
        String output = hbsRenderable.render(new Object(), ImmutableListMultimap.of(), Collections.emptyMap());
        Assert.assertEquals(output, "<head>/my.js/ok.js</head>");
    }
}
