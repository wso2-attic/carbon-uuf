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
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.io.TemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.exception.RenderingException;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.Executable;
import org.wso2.carbon.uuf.renderablecreator.hbs.exception.HbsRenderingException;
import org.wso2.carbon.uuf.renderablecreator.hbs.model.ContextModel;
import org.wso2.carbon.uuf.spi.model.Model;

import java.io.IOException;
import java.util.Map;

import static org.wso2.carbon.uuf.renderablecreator.hbs.internal.serialize.JsonSerializer.toPrettyJson;

public class HbsFragmentRenderable extends HbsPageRenderable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HbsFragmentRenderable.class);

    public HbsFragmentRenderable(TemplateSource templateSource) {
        super(templateSource);
    }

    public HbsFragmentRenderable(TemplateSource templateSource, String path) {
        super(templateSource, path);
    }

    public HbsFragmentRenderable(TemplateSource templateSource, Executable executable) {
        super(templateSource, executable);
    }

    public HbsFragmentRenderable(TemplateSource templateSource, String absolutePath, String relativePath,
                                 Executable executable) {
        super(templateSource, absolutePath, relativePath, executable);
    }

    @Override
    public String render(Model model, Lookup lookup, RequestLookup requestLookup, API api) throws RenderingException {
        Context context;
        Executable executable = getExecutable();
        if (executable == null) {
            Map<String, Object> templateModel = getTemplateModel(model, lookup, requestLookup, api);
            if (model instanceof ContextModel) {
                context = Context.newContext(((ContextModel) model).getParentContext(), templateModel);
            } else {
                context = Context.newContext(templateModel);
            }
        } else {
            Map executeOutput = execute(executable, getExecutableContext(model, lookup, requestLookup), api, lookup,
                                        requestLookup);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Executable output \"" + toPrettyJson(executeOutput) + "\".");
            }
            if (model instanceof ContextModel) {
                context = Context.newContext(((ContextModel) model).getParentContext(), executeOutput);
            } else {
                context = Context.newContext(executeOutput);
            }
            context.combine(getTemplateModel(model, lookup, requestLookup, api));
        }

        context.data(DATA_KEY_LOOKUP, lookup);
        context.data(DATA_KEY_REQUEST_LOOKUP, requestLookup);
        context.data(DATA_KEY_API, api);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Template \"" + this + "\" will be applied with context \"" + toPrettyJson(context) + "\".");
        }
        try {
            return getTemplate().apply(context);
        } catch (IOException e) {
            throw new HbsRenderingException("Cannot load fragment Handlebars template '" + getAbsolutePath() + "'.", e);
        } catch (HandlebarsException e) {
            throw new HbsRenderingException("Cannot render fragment Handlebars template '" + getAbsolutePath() + "'.",
                                            e);
        }
    }
}
