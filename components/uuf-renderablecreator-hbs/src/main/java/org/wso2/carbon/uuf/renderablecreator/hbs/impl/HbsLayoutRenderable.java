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

package org.wso2.carbon.uuf.renderablecreator.hbs.impl;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.internal.io.PlaceholderWriter;
import org.wso2.carbon.uuf.spi.model.Model;

import java.io.IOException;

public class HbsLayoutRenderable extends HbsRenderable {

    private final Template template;
    private final String absolutePath;
    private final String relativePath;

    public HbsLayoutRenderable(TemplateSource templateSource) {
        this(templateSource, null, null);
    }

    public HbsLayoutRenderable(TemplateSource templateSource, String path) {
        this(templateSource, path, null);
    }

    public HbsLayoutRenderable(TemplateSource templateSource, String absolutePath, String relativePath) {
        this.template = compile(templateSource);
        this.absolutePath = absolutePath;
        this.relativePath = relativePath;
    }

    @Override
    protected Template getTemplate() {
        return template;
    }

    @Override
    protected String getAbsolutePath() {
        return absolutePath;
    }

    @Override
    protected String getRelativePath() {
        return relativePath;
    }

    @Override
    public String render(Model model, Lookup lookup, RequestLookup requestLookup, API api) {
        Context context = Context.newContext(getTemplateModel(model, lookup, requestLookup, api));
        context.data(DATA_KEY_LOOKUP, lookup);
        context.data(DATA_KEY_REQUEST_LOOKUP, requestLookup);
        context.data(DATA_KEY_API, api);
        PlaceholderWriter writer = new PlaceholderWriter();
        context.data(DATA_KEY_CURRENT_WRITER, writer);
        try {
            getTemplate().apply(context, writer);
        } catch (IOException e) {
            throw new UUFException("An error occurred when rendering the compiled Handlebars template of layout '" +
                                           getAbsolutePath() + "'.", e);
        }
        String out = writer.toString(requestLookup.getPlaceholderContents());
        writer.close();
        return out;
    }
}
