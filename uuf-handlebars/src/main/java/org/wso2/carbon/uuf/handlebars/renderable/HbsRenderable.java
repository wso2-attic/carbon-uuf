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

package org.wso2.carbon.uuf.handlebars.renderable;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableMap;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.handlebars.helpers.FillPlaceholderHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.CssHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.DefinePlaceholderHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.DefineZoneHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.FillZoneHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.FragmentHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.HeadJsHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.HeadOtherHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.JsHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.MenuHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.MissingHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.PublicHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.SecuredHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.TitleHelper;
import org.wso2.carbon.uuf.spi.Renderable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class HbsRenderable implements Renderable {

    public static final String DATA_KEY_LOOKUP = HbsRenderable.class.getName() + "#lookup";
    public static final String DATA_KEY_REQUEST_LOOKUP = HbsRenderable.class.getName() + "#request-lookup";
    public static final String DATA_KEY_API = HbsRenderable.class.getName() + "#api";
    public static final String DATA_KEY_CURRENT_WRITER = HbsRenderable.class.getName() + "#writer";
    //
    protected static final Handlebars HANDLEBARS = new Handlebars();
    private static final Map<String, FillPlaceholderHelper> PLACEHOLDER_HELPERS = ImmutableMap.of(
            CssHelper.HELPER_NAME, new CssHelper(),
            HeadJsHelper.HELPER_NAME, new HeadJsHelper(),
            JsHelper.HELPER_NAME, new JsHelper(),
            TitleHelper.HELPER_NAME, new TitleHelper(),
            HeadOtherHelper.HELPER_NAME, new HeadOtherHelper());

    static {
        HANDLEBARS.registerHelper(MenuHelper.HELPER_NAME, new MenuHelper());
        HANDLEBARS.registerHelper(DefineZoneHelper.HELPER_NAME, new DefineZoneHelper());
        HANDLEBARS.registerHelper(FillZoneHelper.HELPER_NAME, new FillZoneHelper());
        HANDLEBARS.registerHelper(FragmentHelper.HELPER_NAME, new FragmentHelper());
        HANDLEBARS.registerHelper(DefinePlaceholderHelper.HELPER_NAME, new DefinePlaceholderHelper());
        PLACEHOLDER_HELPERS.forEach(HANDLEBARS::registerHelper);
        HANDLEBARS.registerHelper(PublicHelper.HELPER_NAME, new PublicHelper());
        HANDLEBARS.registerHelper(SecuredHelper.HELPER_NAME, new SecuredHelper());
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

    protected Map<String, Object> getHbsModel(Lookup lookup, RequestLookup requestLookup, API api) {
        Map<String, Object> context = new HashMap<>();
        context.put("@app",
                    ImmutableMap.of("context", requestLookup.getAppContext(), "config", lookup.getConfiguration()));
        context.put("@user", api.getSession().map(Session::getUser).orElse(null));
        context.put("@uriParams", requestLookup.getUriParams());
        context.put("@queryParams", requestLookup.getRequest().getQueryParams());
        return context;
    }
}
