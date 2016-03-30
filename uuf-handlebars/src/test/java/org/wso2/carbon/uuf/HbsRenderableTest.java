package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.handlebars.Executable;
import org.wso2.carbon.uuf.handlebars.HbsRenderable;
import org.wso2.carbon.uuf.model.Model;

import java.util.Collections;
import java.util.Map;
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
        String output = hbsRenderable.render("/url", mock(Model.class), mock(Lookup.class));
        Assert.assertEquals(output, templateContent);
    }

    @Test
    public void testTemplateWithModel() {
        HbsRenderable hbsRenderable = createHbsRenderable("Hello {{name}}! Have a good day.");
        Model model = mock(Model.class);
        when(model.toMap()).thenReturn(ImmutableMap.of("name", "Alice"));
        String output = hbsRenderable.render("/url", model, mock(Lookup.class));
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testTemplateWithExecutable() {
        Executable executable = context -> ImmutableMap.of("name", "Alice");
        Model emptyModel = new Model() {
            private Map<String, Object> map = Collections.emptyMap();

            @Override
            public void combine(Map<String, Object> other) {
                map = other;
            }

            @Override
            public Map<String, Object> toMap() {
                return map;
            }
        };
        HbsRenderable hbsRenderable = createHbsRenderable("Hello {{name}}! Have a good day.", executable);
        String output = hbsRenderable.render("/url", emptyModel, mock(Lookup.class));
        Assert.assertEquals(output, "Hello Alice! Have a good day.");
    }

    @Test
    public void testFragmentInclude() {
        HbsRenderable hbsRenderable = createHbsRenderable("X {{includeFragment \"test-fragment\"}} Y");
        Lookup lookup = mock(Lookup.class);
        Fragment fragment = mock(Fragment.class);
        when(fragment.render(any(), any(), any())).thenReturn("fragment content");
        when(lookup.lookupFragment("test-fragment")).thenReturn(fragment);
        String output = hbsRenderable.render("/url", mock(Model.class), lookup);
        Assert.assertEquals(output, "X fragment content Y");
    }

    @Test
    public void testFragmentBind() {
        HbsRenderable hbsRenderable = createHbsRenderable("X {{defineZone \"test-zone\"}} Y");
        Lookup lookup = mock(Lookup.class);
        Renderable renderable = mock(Renderable.class);
        when(renderable.render(any(), any(), any())).thenReturn("fragment content");
        when(lookup.lookupBinding("test-zone")).thenReturn(Collections.singleton(renderable));
        String output = hbsRenderable.render("/url", mock(Model.class), lookup);
        Assert.assertEquals(output, "X fragment content Y");
    }
}
