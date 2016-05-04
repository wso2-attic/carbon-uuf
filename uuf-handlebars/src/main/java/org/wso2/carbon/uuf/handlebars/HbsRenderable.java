/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableMap;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.core.exception.UUFException;
import org.wso2.carbon.uuf.handlebars.helpers.FillPlaceholderHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.CssHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.DefinePlaceholderHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.DefineZoneHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.FillZoneHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.HeaderOtherHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.HeaderTitleHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.IncludeFragmentHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.JsHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.MissingHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.PublicHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class HbsRenderable implements Renderable {

    public static final String DATA_KEY_LOOKUP = HbsRenderable.class.getName() + "#lookup";
    public static final String DATA_KEY_REQUEST_LOOKUP = HbsRenderable.class.getName() + "#request-lookup";
    public static final String DATA_KEY_API = HbsRenderable.class.getName() + "#api";
    public static final String DATA_KEY_CURRENT_WRITER = HbsRenderable.class.getName() + "#writer";
    //
    private static final Handlebars HANDLEBARS = new Handlebars();
    private static final Map<String, FillPlaceholderHelper> PLACEHOLDER_HELPERS = ImmutableMap.of(
            CssHelper.HELPER_NAME, new CssHelper(),
            JsHelper.HELPER_NAME_HEADER, new JsHelper(JsHelper.HELPER_NAME_HEADER),
            JsHelper.HELPER_NAME_FOOTER, new JsHelper(JsHelper.HELPER_NAME_FOOTER),
            HeaderTitleHelper.HELPER_NAME, new HeaderTitleHelper(),
            HeaderOtherHelper.HELPER_NAME, new HeaderOtherHelper());

    static {
        HANDLEBARS.registerHelper(DefineZoneHelper.HELPER_NAME, new DefineZoneHelper());
        HANDLEBARS.registerHelper(FillZoneHelper.HELPER_NAME, new FillZoneHelper());
        HANDLEBARS.registerHelper(IncludeFragmentHelper.HELPER_NAME, new IncludeFragmentHelper());
        HANDLEBARS.registerHelper(DefinePlaceholderHelper.HELPER_NAME, new DefinePlaceholderHelper());
        PLACEHOLDER_HELPERS.forEach(HANDLEBARS::registerHelper);
        HANDLEBARS.registerHelper(PublicHelper.HELPER_NAME, new PublicHelper());
        HANDLEBARS.registerHelperMissing(new MissingHelper());
    }

    protected final Template compiledTemplate;
    protected final String templatePath;

    public HbsRenderable(TemplateSource template) {
        this.templatePath = template.filename();
        try {
            this.compiledTemplate = HANDLEBARS.compile(template);
        } catch (IOException e) {
            throw new UUFException("Cannot compile Handlebars template '" + templatePath + "'.", e);
        }
    }

    protected Map<String, Object> getHbsModel(ComponentLookup lookup, RequestLookup requestLookup) {
        Map<String, Object> context = new HashMap<>();
        context.put("@uriParams", requestLookup.getUriParams());
        context.put("@app",
                    ImmutableMap.of("context", requestLookup.getAppContext(), "config", lookup.getConfigurations()));
        return context;
    }
}
