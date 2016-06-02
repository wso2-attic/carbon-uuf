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

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.io.TemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.handlebars.DebugUtil;
import org.wso2.carbon.uuf.handlebars.Executable;
import org.wso2.carbon.uuf.handlebars.model.ContextModel;
import org.wso2.carbon.uuf.spi.model.Model;

import java.io.IOException;
import java.util.Map;

public class HbsFragmentRenderable extends HbsPageRenderable {

    private static final Logger log = LoggerFactory.getLogger(HbsFragmentRenderable.class);

    public HbsFragmentRenderable(TemplateSource template) {
        super(template);
    }

    public HbsFragmentRenderable(TemplateSource template, Executable executable) {
        super(template, executable);
    }

    @Override
    public String render(Model model, ComponentLookup lookup, RequestLookup requestLookup, API api) {
        Context context;
        if (executable == null) {
            Map<String, Object> hbsModel = getHbsModel(model, lookup, requestLookup);
            if (model instanceof ContextModel) {
                context = Context.newContext(((ContextModel) model).getParentContext(), hbsModel);
            } else {
                context = Context.newContext(hbsModel);
            }
        } else {
            Object executableOutput = executeExecutable(getExecutableContext(model, lookup, requestLookup), api);
            if (log.isDebugEnabled()) {
                log.debug("Executable output \"" + DebugUtil.safeJsonString(executableOutput) + "\".");
            }
            if (model instanceof ContextModel) {
                context = Context.newContext(((ContextModel) model).getParentContext(), executableOutput);
            } else {
                context = Context.newContext(executableOutput);
            }
            context.combine(getHbsModel(model, lookup, requestLookup));
        }

        context.data(DATA_KEY_LOOKUP, lookup);
        context.data(DATA_KEY_REQUEST_LOOKUP, requestLookup);
        context.data(DATA_KEY_API, api);
        if (log.isDebugEnabled()) {
            log.debug("Template \"" + this + "\" will be applied with context \"" + DebugUtil.safeJsonString(context) +
                              "\".");
        }
        try {
            return compiledTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("An error occurred when writing to the in-memory PlaceholderWriter.", e);
        }
    }

    private Map<String, Object> getExecutableContext(Model model, ComponentLookup lookup, RequestLookup requestLookup) {
        Map<String, Object> context = getExecutableContext(lookup, requestLookup);
        context.put("params", model.toMap());
        return context;
    }

    private Map<String, Object> getHbsModel(Model model, ComponentLookup lookup, RequestLookup requestLookup) {
        Map<String, Object> context = getHbsModel(lookup, requestLookup);
        context.put("@params", model.toMap());
        return context;
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + templatePath + "\"" + (executable == null ? "}" : ", \"js\": " + executable + "}");
    }
}
