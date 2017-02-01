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
import com.github.jknack.handlebars.io.TemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.exception.InvalidTypeException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.Executable;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.internal.DebugUtil;
import org.wso2.carbon.uuf.renderablecreator.hbs.internal.io.PlaceholderWriter;
import org.wso2.carbon.uuf.spi.model.Model;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HbsPageRenderable extends HbsRenderable {

    private static final Logger log = LoggerFactory.getLogger(HbsPageRenderable.class);

    private final Executable executable;

    public HbsPageRenderable(TemplateSource templateSource) {
        this(templateSource, null, null, null);
    }

    public HbsPageRenderable(TemplateSource templateSource, String path) {
        this(templateSource, path, null, null);
    }

    public HbsPageRenderable(TemplateSource templateSource, Executable executable) {
        this(templateSource, null, null, executable);
    }

    public HbsPageRenderable(TemplateSource templateSource, String absolutePath, String relativePath,
                             Executable executable) {
        super(templateSource, absolutePath, relativePath);
        this.executable = executable;
    }

    protected Executable getExecutable() {
        return executable;
    }

    @Override
    public String render(Model model, Lookup lookup, RequestLookup requestLookup, API api) {
        Context context;
        Executable executable = getExecutable();
        if (executable == null) {
            context = Context.newContext(getTemplateModel(model, lookup, requestLookup, api));
        } else {
            Map executeOutput = execute(executable, getExecutableContext(model, lookup, requestLookup), api, lookup,
                                        requestLookup);
            if (log.isDebugEnabled()) {
                log.debug("Executable output \"" + DebugUtil.safeJsonString(executeOutput) + "\".");
            }
            context = Context.newContext(executeOutput).combine(getTemplateModel(model, lookup, requestLookup, api));
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
            getTemplate().apply(context, writer);
        } catch (IOException e) {
            throw new UUFException("An error occurred when writing to the in-memory PlaceholderWriter.", e);
        }
        String out = writer.toString(requestLookup.getPlaceholderContents());
        writer.close();
        return out;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAbsolutePath(), getTemplate(), getExecutable());
    }

    @Override
    public String toString() {
        return "{\"path\": {\"absolute\": \"" + getAbsolutePath() + "\", \"relative\": \"" + getRelativePath() +
                "\"}, \"js\": " + getExecutable() + "}";
    }

    protected static Map<String, Object> getExecutableContext(Model model, Lookup lookup, RequestLookup requestLookup) {
        Map<String, Object> context = new HashMap<>();
        context.put("contextPath", requestLookup.getContextPath());
        context.put("config", lookup.getConfiguration().other());
        context.put("request", requestLookup.getRequest());
        context.put("response", requestLookup.getResponse());
        context.put("pathParams", requestLookup.getPathParams());
        context.put("params", ((model == null) ? null : model.toMap()));
        return context;
    }

    protected static Map execute(Executable executable, Object context, API api, Lookup lookup,
                                 RequestLookup requestLookup) {
        Object executableOutput = executable.execute(context, api, lookup, requestLookup);
        if (executableOutput == null) {
            return Collections.emptyMap();
        }
        if ((executableOutput instanceof Map)) {
            return (Map) executableOutput;
        } else {
            throw new InvalidTypeException("Expected a Map as the output from executing the executable '" + executable +
                                                   "'. Instead found '" + executableOutput.getClass().getName() + "'.");
        }
    }
}
