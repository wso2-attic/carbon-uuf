package org.wso2.carbon.uuf;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedSet;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.model.Model;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

public class ComponentTest {

    private static Page createPage(String uriPattern, String content) {
        return new Page(new UriPatten(uriPattern), null) {
            @Override
            public String render(Model model, ComponentLookup componentLookup, RequestLookup requestLookup, API api) {
                return content;
            }
        };
    }

    private static ComponentLookup createComponentLookup(String componentName) {
        return new ComponentLookup(componentName, "/componentContext", Collections.emptySet(), Collections.emptySet(),
                                   ImmutableSetMultimap.of(), Collections.emptySet());
    }

    private static RequestLookup createRequestLookup() {
        return new RequestLookup("/appContext", any());
    }

    @Test
    public void testRenderExistingPage() {
        Page p1 = createPage("/test/page/one", "Hello world from test page one!");
        Page p2 = createPage("/test/page/two", "Hello world from test page two!");
        ComponentLookup lookup = createComponentLookup("componentName");
        Component component = new Component("componentName", anyString(), ImmutableSortedSet.of(p1, p2), lookup);
        new RequestLookup("/", null);

        Optional<String> output = component.renderPage("/test/page/one", createRequestLookup(), any());
        Assert.assertEquals(output.get(), "Hello world from test page one!");
    }

    @Test
    public void testRenderExistingPageWithWildcard() {
        Page p1 = createPage("/test/page/{wildcard}/one", "Hello world from test page one!");
        Page p2 = createPage("/test/page/no-wildcard/two", "Hello world from test page two!");
        ComponentLookup lookup = createComponentLookup("componentName");
        Component component = new Component("componentName", anyString(), ImmutableSortedSet.of(p1, p2), lookup);

        //TODO: fix bug in URiPattern class, if there is a '-' in the wildcard value then matching returns false.
        Optional<String> output = component.renderPage("/test/page/wildcard-value/one", createRequestLookup(), any());
        Assert.assertEquals(output.get(), "Hello world from test page one!");
    }

    @Test
    public void testRenderNonExistingPage() {
        Page p1 = createPage("/test/page/one", anyString());
        Page p2 = createPage("/test/page/two", anyString());
        ComponentLookup lookup = createComponentLookup("componentName");
        Component component = new Component("componentName", anyString(), ImmutableSortedSet.of(p1, p2), lookup);

        Optional<String> output = component.renderPage("/test/page/three", createRequestLookup(), any());
        Assert.assertFalse(output.isPresent());
    }
}
