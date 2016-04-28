package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.handlebars.Executable;
import org.wso2.carbon.uuf.handlebars.HbsFragmentRenderable;
import org.wso2.carbon.uuf.handlebars.HbsPageRenderable;
import org.wso2.carbon.uuf.handlebars.HbsRenderable;
import org.wso2.carbon.uuf.model.MapModel;
import org.wso2.carbon.uuf.model.Model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HbsPageRenderableTest {

    private static HbsRenderable createHbsRenderable(String sourceStr) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsPageRenderable(stringTemplateSource);
    }

    private static HbsRenderable createHbsRenderable(String sourceStr, Executable executable) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsFragmentRenderable(stringTemplateSource, executable);
    }

    private static Model createEmptyModel() {
        Model model = mock(Model.class);
        when(model.toMap()).thenReturn(Collections.emptyMap());
        return model;
    }

    @Test
    public void testTemplate() {
        final String templateContent = "A Plain Handlebars template.";

        HbsRenderable hbsRenderable = createHbsRenderable(templateContent);

        String output = hbsRenderable.render(createEmptyModel(), null, null, null);
        Assert.assertEquals(output, templateContent);
    }

    @Test
    public void testTemplateWithModel() {
        HbsRenderable hbsRenderable = createHbsRenderable("Hello {{name}}! Have a good day.");
        Model model = new MapModel(ImmutableMap.of("name", "Alice"));

        String output = hbsRenderable.render(model, any(), any(), any());
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testTemplateWithExecutable() {
        Executable executable = (context, api) -> ImmutableMap.of("name", "Alice");
        HbsRenderable hbsRenderable = createHbsRenderable("Hello {{name}}! Have a good day.", executable);
        Model model = new MapModel(new HashMap<>());

        String output = hbsRenderable.render(model, null, null, null);
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testFragmentInclude() {
        HbsRenderable hbsRenderable = createHbsRenderable("X {{includeFragment \"test-fragment\"}} Y");
        Fragment fragment = mock(Fragment.class);
        when(fragment.render(any(), any(), any(), any())).thenReturn("fragment content");

        ComponentLookup lookup = mock(ComponentLookup.class);
        when(lookup.getFragment("test-fragment")).thenReturn(Optional.of(fragment));

        String output = hbsRenderable.render(createEmptyModel(), lookup, null, null);
        Assert.assertEquals(output, "X fragment content Y");
    }

    @Test
    public void testFragmentBind() {
        HbsRenderable hbsRenderable = createHbsRenderable("X {{defineZone \"test-zone\"}} Y");
        ComponentLookup lookup = mock(ComponentLookup.class);
        Fragment pushedFragment = mock(Fragment.class);
        when(pushedFragment.render(any(), any(), any(), any())).thenReturn("fragment content");
        when(lookup.getBindings("test-zone")).thenReturn(ImmutableSet.of(pushedFragment));
        RequestLookup requestLookup = mock(RequestLookup.class);
        when(requestLookup.getZoneContent("test-zone")).thenReturn(Optional.<String>empty());

        String output = hbsRenderable.render(createEmptyModel(), lookup, requestLookup, null);
        Assert.assertEquals(output, "X fragment content Y");
    }

    @Test
    public void testzone() {
        HbsRenderable hbsRenderable = createHbsRenderable("X {{defineZone \"test-zone\"}} Y");
        ComponentLookup lookup = mock(ComponentLookup.class);
        Fragment pushedFragment = mock(Fragment.class);
        when(pushedFragment.render(any(), any(), any(), any())).thenReturn("fragment content");
        when(lookup.getBindings("test-zone")).thenReturn(ImmutableSet.of(pushedFragment));

        String output = hbsRenderable.render(createEmptyModel(), lookup, null, null);
        Assert.assertEquals(output, "X fragment content Y");
    }

    @Test
    public void testPublicHelper() {
        final String templateContent = "{{public \"/relative/path\"}}";
        HbsRenderable hbsRenderable = createHbsRenderable(templateContent);
        RequestLookup requestLookup = mock(RequestLookup.class);
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/mycomponent/base");

        String output = hbsRenderable.render(createEmptyModel(), null, requestLookup, null);
        Assert.assertEquals(output, "/myapp/public/mycomponent/base/relative/path");
    }
}
