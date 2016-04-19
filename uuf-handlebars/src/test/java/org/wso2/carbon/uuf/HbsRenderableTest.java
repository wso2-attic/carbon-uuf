package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.handlebars.Executable;
import org.wso2.carbon.uuf.handlebars.HbsRenderable;
import org.wso2.carbon.uuf.model.Model;

import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HbsRenderableTest {

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

        String output = hbsRenderable.render(any(), any(), any(), any());
        Assert.assertEquals(output, templateContent);
    }

    @Test
    public void testTemplateWithModel() {
        HbsRenderable hbsRenderable = createHbsRenderable("Hello {{name}}! Have a good day.");
        Model model = mock(Model.class);
        when(model.toMap()).thenReturn(ImmutableMap.of("name", "Alice"));

        String output = hbsRenderable.render(model, any(), any(), any());
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testTemplateWithExecutable() {
        Executable executable = context -> ImmutableMap.of("name", "Alice");
        Model model = mock(Model.class);
        when(model.toMap()).thenReturn(ImmutableMap.of("name", "Alice"));
        HbsRenderable hbsRenderable = createHbsRenderable("Hello {{name}}! Have a good day.", executable);

        String output = hbsRenderable.render(model, any(), any(), any());
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testFragmentInclude() {
        HbsRenderable hbsRenderable = createHbsRenderable("X {{includeFragment \"test-fragment\"}} Y");
        ComponentLookup lookup = mock(ComponentLookup.class);
        Fragment fragment = mock(Fragment.class);
        when(fragment.render(any(), any(), any(), any())).thenReturn("fragment content");
        when(lookup.getFragment("test-fragment")).thenReturn(Optional.of(fragment));

        String output = hbsRenderable.render(any(), lookup, any(), any());
        Assert.assertEquals(output, "X fragment content Y");
    }

    @Test
    public void testFragmentBind() {
        HbsRenderable hbsRenderable = createHbsRenderable("X {{defineZone \"test-zone\"}} Y");
        ComponentLookup lookup = mock(ComponentLookup.class);
        Renderable zoneRenderable = mock(Renderable.class);
        when(zoneRenderable.render(any(), any(), any(), any())).thenReturn("zone content");
        when(lookup.getBindings("test-zone")).thenReturn(ImmutableSet.of(zoneRenderable));

        String output = hbsRenderable.render(any(), lookup, any(), any());
        Assert.assertEquals(output, "X zone content Y");
    }

    @Test
    public void testPublicHelper() {
        final String templateContent = "{{public \"/relative/path\"}}";
        HbsRenderable hbsRenderable = createHbsRenderable(templateContent);
        RequestLookup requestLookup = mock(RequestLookup.class);
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/mycomponent/base");

        String output = hbsRenderable.render(any(), any(), requestLookup, any());
        Assert.assertEquals(output, "/myapp/public/mycomponent/base/relative/path");
    }
}
