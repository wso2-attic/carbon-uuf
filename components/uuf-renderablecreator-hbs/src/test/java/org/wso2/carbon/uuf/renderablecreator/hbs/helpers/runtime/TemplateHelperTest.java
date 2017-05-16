/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime;

import com.google.common.collect.ImmutableSortedSet;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.Placeholder;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.core.UriPatten;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createAPI;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createLookup;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createRenderable;
import static org.wso2.carbon.uuf.renderablecreator.hbs.helpers.runtime.RuntimeHelpersTestUtil.createRequestLookup;

/**
 * Testcases for the {@code {{template}}} halper.
 *
 * @since 1.0.0
 */
public class TemplateHelperTest {

    @Test
    public void testInlineTemplate() {
        RequestLookup requestLookup = createRequestLookup();
        String templateName = "testTemplateName";
        String scriptText = "" +
                "<div class=\"col-md-12\">\n" +
                "    {{#each devices}}\n" +
                "        <div class=\"device\">\n" +
                "            <span>Name : {{name}}</span>\n" +
                "            <span>Type : {{type}}</span>\n" +
                "        </div>\n" +
                "    {{/each}}\n" +
                "</div>";

        createRenderable("{{#template \"" + templateName + "\"}}\n" + scriptText + "{{/template}}").
                render(null, createLookup(), requestLookup, createAPI());
        String expected = "<script id=\"" + templateName + "\" type=\"text/x-handlebars-template\">\n" + scriptText +
                "</script>";
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.js).orElse(null), expected);
    }

    @Test
    public void testFragmentTemplate() {
        String templateName = "testTemplateName";
        String componentName = "test.componentName";
        String fragmentName = componentName + ".fragmentName";
        String pageContent = "{{template \"" + templateName + "\" \"" + fragmentName + "\"}}";
        String fragmentContent = "" +
                "<div class=\"col-md-12\">\n" +
                "    {{#each devices}}\n" +
                "        <div class=\"device\">\n" +
                "            <span>Name : {{name}}</span>\n" +
                "            <span>Type : {{type}}</span>\n" +
                "        </div>\n" +
                "    {{/each}}\n" +
                "</div>";
        Page page = new Page(new UriPatten("/contextPath"), createRenderable(pageContent), null);
        Fragment fragment = new Fragment(fragmentName, createRenderable(fragmentContent), null);
        Component component = new Component(componentName, null, null, ImmutableSortedSet.of(page),
                                            Collections.emptySet(), Collections.emptySet(), Collections.emptySet(),
                                            null);
        Lookup lookup = createLookup();
        when(lookup.getFragmentIn(anyString(), anyString())).thenReturn(Optional.of(fragment));
        when(lookup.getComponent(anyString())).thenReturn(Optional.of(component));
        RequestLookup requestLookup = createRequestLookup();

        component.renderPage("/contextPath", null, lookup, requestLookup, createAPI());
        String expected = "<script id=\"" + templateName + "\" type=\"text/x-handlebars-template\">\n" +
                fragmentContent + "\n</script>";
        Assert.assertEquals(requestLookup.getPlaceholderContent(Placeholder.js).orElse(null), expected);
    }
}
