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
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.exception.InvalidTypeException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.handlebars.DebugUtil;
import org.wso2.carbon.uuf.handlebars.Executable;
import org.wso2.carbon.uuf.handlebars.PlaceholderWriter;
import org.wso2.carbon.uuf.spi.model.Model;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HbsPageRenderable extends HbsRenderable {

    private static final Logger log = LoggerFactory.getLogger(HbsPageRenderable.class);

    protected final Executable executable;

    public HbsPageRenderable(TemplateSource template) {
        this(template, null);
    }

    public HbsPageRenderable(TemplateSource template, Executable executable) {
        super(template);
        this.executable = executable;
    }

    @Override
    public String render(Model model, ComponentLookup lookup, RequestLookup requestLookup, API api) {
        Context context;
        if (executable == null) {
            context = Context.newContext(getHbsModel(lookup, requestLookup));
        } else {
            Object executableOutput = executeExecutable(getExecutableContext(lookup, requestLookup), api);
            if (log.isDebugEnabled()) {
                log.debug("Executable output \"" + DebugUtil.safeJsonString(executableOutput) + "\".");
            }
            context = Context.newContext(executableOutput).combine(getHbsModel(lookup, requestLookup));
        }

        context.data(DATA_KEY_LOOKUP, lookup);
        context.data(DATA_KEY_REQUEST_LOOKUP, requestLookup);
        context.data(DATA_KEY_API, api);
        if (log.isDebugEnabled()) {
            log.debug("Template \"" + this + "\" will be applied with context \"" + DebugUtil.safeJsonString(context) +
                              "\".");
        }
        PlaceholderWriter writer = new PlaceholderWriter();
        context.data(DATA_KEY_CURRENT_WRITER, writer);
        try {
            compiledTemplate.apply(context, writer);
        } catch (IOException e) {
            throw new UUFException("An error occurred when writing to the in-memory PlaceholderWriter.", e);
        }
        String out = writer.toString(requestLookup.getPlaceholderContents());
        writer.close();
        return out;
    }

    protected Map executeExecutable(Object context, API api) {
        Object executableOutput = executable.execute(context, api);
        if (executableOutput == null) {
            return Collections.emptyMap();
        }
        if ((executableOutput instanceof Map)) {
            return (Map) executableOutput;
        } else {
            throw new InvalidTypeException(
                    "Expected a Map as the output from executing the executable '" + executable +
                            "'. Instead found '" + executableOutput.getClass().getName() + "'.");
        }
    }

    protected Map<String, Object> getExecutableContext(ComponentLookup lookup, RequestLookup requestLookup) {
        Map<String, Object> context = new HashMap<>();
        context.put("request", requestLookup.getRequest());
        context.put("uriParams", requestLookup.getUriParams());
        context.put("app",
                    ImmutableMap.of("context", requestLookup.getAppContext(), "config", lookup.getConfigurations()));
        return context;
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + templatePath + "\"" + (executable == null ? "}" : ", \"js\": " + executable + "}");
    }
}
