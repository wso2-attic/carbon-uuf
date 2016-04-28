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
import org.wso2.carbon.uuf.handlebars.HbsPageRenderable;
import org.wso2.carbon.uuf.model.MapModel;
import org.wso2.carbon.uuf.model.Model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HbsPageRenderableTest {

    private static HbsPageRenderable createPageRenderable(String sourceStr) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsPageRenderable(stringTemplateSource);
    }

    private static HbsPageRenderable createPageRenderable(String sourceStr, Executable executable) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsPageRenderable(stringTemplateSource, executable);
    }

    private static Model createEmptyModel() {
        Model model = mock(Model.class);
        when(model.toMap()).thenReturn(Collections.emptyMap());
        return model;
    }

    private static RequestLookup createEmptyRequestLookup() {
        RequestLookup requestLookup = mock(RequestLookup.class);
        when(requestLookup.getZoneContent(anyString())).thenReturn(Optional.<String>empty());
        when(requestLookup.getPlaceholderContents()).thenReturn(Collections.<String, String>emptyMap());
        when(requestLookup.getAppContext()).thenReturn("/myapp");
        return requestLookup;
    }

    @Test
    public void testTemplate() {
        final String templateContent = "A Plain Handlebars template.";
        HbsPageRenderable pageRenderable = createPageRenderable(templateContent);

        String output = pageRenderable.render(createEmptyModel(), null, createEmptyRequestLookup(), null);
        Assert.assertEquals(output, templateContent);
    }

    @Test
    public void testTemplateWithExecutable() {
        Executable executable = (context, api) -> ImmutableMap.of("name", "Alice");
        HbsPageRenderable pageRenderable = createPageRenderable("Hello {{name}}! Have a good day.", executable);
        Model model = new MapModel(new HashMap<>());

        String output = pageRenderable.render(model, null, createEmptyRequestLookup(), null);
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testFragmentInclude() {
        HbsPageRenderable pageRenderable = createPageRenderable("X {{includeFragment \"test-fragment\"}} Y");
        Fragment fragment = mock(Fragment.class);
        when(fragment.render(any(), any(), any(), any())).thenReturn("fragment content");
        ComponentLookup lookup = mock(ComponentLookup.class);
        when(lookup.getFragment("test-fragment")).thenReturn(Optional.of(fragment));

        String output = pageRenderable.render(createEmptyModel(), lookup, createEmptyRequestLookup(), null);
        Assert.assertEquals(output, "X fragment content Y");
    }

    @Test
    public void testFragmentBinding() {
        HbsPageRenderable pageRenderable = createPageRenderable("X {{defineZone \"test-zone\"}} Y");
        ComponentLookup lookup = mock(ComponentLookup.class);
        Fragment pushedFragment = mock(Fragment.class);
        when(pushedFragment.render(any(), any(), any(), any())).thenReturn("fragment content");
        when(lookup.getBindings("test-zone")).thenReturn(ImmutableSet.of(pushedFragment));

        String output = pageRenderable.render(createEmptyModel(), lookup, createEmptyRequestLookup(), null);
        Assert.assertEquals(output, "X fragment content Y");
    }

    @Test
    public void testZone() {
        HbsPageRenderable pageRenderable = createPageRenderable("X {{defineZone \"test-zone\"}} Y");
        ComponentLookup lookup = mock(ComponentLookup.class);
        when(lookup.getBindings(anyString())).thenReturn(Collections.emptySet());
        RequestLookup requestLookup = createEmptyRequestLookup();
        when(requestLookup.getZoneContent("test-zone")).thenReturn(Optional.of("zone content"));

        String output = pageRenderable.render(createEmptyModel(), lookup, requestLookup, null);
        Assert.assertEquals(output, "X zone content Y");
    }

    @Test
    public void testPublicHelper() {
        final String templateContent = "{{public \"/relative/path\"}}";
        HbsPageRenderable pageRenderable = createPageRenderable(templateContent);
        RequestLookup requestLookup = createEmptyRequestLookup();
        when(requestLookup.getPublicUri()).thenReturn("/myapp/public/mycomponent/base");

        String output = pageRenderable.render(createEmptyModel(), null, requestLookup, null);
        Assert.assertEquals(output, "/myapp/public/mycomponent/base/relative/path");
    }
}
