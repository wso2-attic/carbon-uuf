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

package org.wso2.carbon.uuf.renderablecreator.hbs.renderable;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.io.TemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.Executable;
import org.wso2.carbon.uuf.renderablecreator.hbs.internal.DebugUtil;
import org.wso2.carbon.uuf.renderablecreator.hbs.model.ContextModel;
import org.wso2.carbon.uuf.spi.model.Model;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class HbsFragmentRenderable extends HbsPageRenderable {

    private static final Logger log = LoggerFactory.getLogger(HbsFragmentRenderable.class);

    protected HbsFragmentRenderable() {
    }

    public HbsFragmentRenderable(TemplateSource template) {
        super(template);
    }

    public HbsFragmentRenderable(TemplateSource template, Executable executable) {
        super(template, executable);
    }

    @Override
    public String render(Model model, Lookup lookup, RequestLookup requestLookup, API api) {
        Context context;
        Optional<Executable> executable = getExecutable();
        if (executable.isPresent()) {
            Map executeOutput = execute(executable.get(), getExecutableContext(model, lookup, requestLookup), api);
            if (log.isDebugEnabled()) {
                log.debug("Executable output \"" + DebugUtil.safeJsonString(executeOutput) + "\".");
            }
            if (model instanceof ContextModel) {
                context = Context.newContext(((ContextModel) model).getParentContext(), executeOutput);
            } else {
                context = Context.newContext(executeOutput);
            }
            context.combine(getTemplateModel(model, lookup, requestLookup, api));
        } else {
            Map<String, Object> templateModel = getTemplateModel(model, lookup, requestLookup, api);
            if (model instanceof ContextModel) {
                context = Context.newContext(((ContextModel) model).getParentContext(), templateModel);
            } else {
                context = Context.newContext(templateModel);
            }
        }

        context.data(DATA_KEY_LOOKUP, lookup);
        context.data(DATA_KEY_REQUEST_LOOKUP, requestLookup);
        context.data(DATA_KEY_API, api);
        if (log.isDebugEnabled()) {
            log.debug("Template \"" + this + "\" will be applied with context \"" + DebugUtil.safeJsonString(context) +
                              "\".");
        }
        try {
            return getTemplate().apply(context);
        } catch (IOException e) {
            throw new UUFException("An error occurred when writing to the in-memory PlaceholderWriter.", e);
        }
    }
}
