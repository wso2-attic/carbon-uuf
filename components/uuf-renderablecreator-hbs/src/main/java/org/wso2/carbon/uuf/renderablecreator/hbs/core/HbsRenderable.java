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

package org.wso2.carbon.uuf.renderablecreator.hbs.core;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.renderablecreator.hbs.helpers.registry.HbsHelperRegistry;
import org.wso2.carbon.uuf.spi.Renderable;
import org.wso2.carbon.uuf.spi.model.Model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class HbsRenderable implements Renderable {

    public static final String DATA_KEY_LOOKUP = HbsRenderable.class.getName() + "#lookup";
    public static final String DATA_KEY_REQUEST_LOOKUP = HbsRenderable.class.getName() + "#request-lookup";
    public static final String DATA_KEY_API = HbsRenderable.class.getName() + "#api";
    public static final String DATA_KEY_CURRENT_WRITER = HbsRenderable.class.getName() + "#writer";
    private static final Handlebars HANDLEBARS = new Handlebars().with(new HbsHelperRegistry());

    private final Template template;
    private final String absolutePath;
    private final String relativePath;

    public HbsRenderable(TemplateSource templateSource, String absolutePath, String relativePath) {
        this.template = (templateSource != null) ? compile(templateSource) : null;
        this.absolutePath = absolutePath;
        this.relativePath = relativePath;
    }

    public Template getTemplate() {
        return template;
    }

    protected String getAbsolutePath() {
        return absolutePath;
    }

    protected String getRelativePath() {
        return relativePath;
    }

    @Override
    public int hashCode() {
        return Objects.hash(absolutePath, getTemplate());
    }

    @Override
    public String toString() {
        return "{\"path\": {\"absolute\": \"" + absolutePath + "\", \"relative\": \"" + relativePath + "\"}}";
    }

    protected static Template compile(TemplateSource templateSource) {
        try {
            return HANDLEBARS.compile(templateSource);
        } catch (IOException e) {
            throw new UUFException("Cannot load Handlebars template '" + templateSource.filename() + "'.", e);
        } catch (HandlebarsException e) {
            throw new UUFException("Cannot compile Handlebars template '" + templateSource.filename() + "'.", e);
        }
    }

    protected static Map<String, Object> getTemplateModel(Model model, Lookup lookup, RequestLookup requestLookup,
                                                          API api) {
        Map<String, Object> context = new HashMap<>();
        context.put("@contextPath", requestLookup.getContextPath());
        context.put("@config", lookup.getConfiguration().asMap());
        context.put("@user", api.getSession().map(session -> (Object) session.getUser()).orElse(false));
        context.put("@pathParams", requestLookup.getPathParams());
        context.put("@queryParams", requestLookup.getRequest().getQueryParams());
        context.put("@params", ((model == null) ? false : model.toMap()));
        return context;
    }
}
