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

import com.github.jknack.handlebars.io.StringTemplateSource;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.HbsPageRenderable;
import org.wso2.carbon.uuf.spi.HttpRequest;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Utility methods for the runtime Handlebars helpers test classes.
 *
 * @since 1.0.0
 */
public class RuntimeHelpersTestUtil {

    public static HbsRenderable createRenderable(String sourceStr) {
        StringTemplateSource stringTemplateSource = new StringTemplateSource("<test-source>", sourceStr);
        return new HbsPageRenderable(stringTemplateSource);
    }

    public static Lookup createLookup() {
        Lookup lookup = mock(Lookup.class);
        Configuration configuration = createConfiguration();
        when(lookup.getConfiguration()).thenReturn(configuration);
        return lookup;
    }

    public static Configuration createConfiguration() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.other()).thenReturn(Collections.emptyMap());
        return configuration;
    }

    public static RequestLookup createRequestLookup() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getQueryParams()).thenReturn(Collections.emptyMap());
        return spy(new RequestLookup("/contextPath", request, null));
    }

    public static API createAPI() {
        API api = mock(API.class);
        when(api.getSession()).thenReturn(Optional.empty());
        return api;
    }

    public static RequestLookup.RenderingFlowTracker createRenderingFlowTracker(boolean isInFragment) {
        RequestLookup.RenderingFlowTracker tracker = mock(RequestLookup.RenderingFlowTracker.class);
        if (isInFragment) {
            when(tracker.isInFragment()).thenReturn(true);
        } else {
            when(tracker.isInPage()).thenReturn(true);
            when(tracker.isInLayout()).thenReturn(true);
        }
        return tracker;
    }
}
