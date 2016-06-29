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
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.exception.InvalidTypeException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.renderablecreator.hbs.PlaceholderWriter;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.Executable;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.HbsRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.internal.DebugUtil;
import org.wso2.carbon.uuf.spi.model.Model;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class HbsPageRenderable extends HbsRenderable {

    private static final Logger log = LoggerFactory.getLogger(HbsPageRenderable.class);

    private final String path;
    private final Template template;
    private final Executable executable;

    protected HbsPageRenderable() {
        this.path = null;
        this.template = null;
        this.executable = null;
    }

    public HbsPageRenderable(TemplateSource templateSource) {
        this(templateSource, null);
    }

    public HbsPageRenderable(TemplateSource templateSource, Executable executable) {
        this.path = templateSource.filename();
        this.template = compile(templateSource);
        this.executable = executable;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Template getTemplate() {
        return template;
    }

    public Optional<Executable> getExecutable() {
        return Optional.ofNullable(executable);
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
            context = Context.newContext(executeOutput).combine(getTemplateModel(model, lookup, requestLookup, api));
        } else {
            context = Context.newContext(getTemplateModel(model, lookup, requestLookup, api));
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

    protected Map<String, Object> getExecutableContext(Model model, Lookup lookup, RequestLookup requestLookup) {
        Map<String, Object> context = new HashMap<>();
        context.put("contextPath", requestLookup.getContextPath());
        context.put("config", lookup.getConfiguration());
        context.put("request", requestLookup.getRequest());
        context.put("response", requestLookup.getResponse());
        context.put("pathParams", requestLookup.getPathParams());
        context.put("params", ((model == null) ? null : model.toMap()));
        return context;
    }

    protected Map execute(Executable executable, Object context, API api) {
        Object executableOutput = executable.execute(context, api);
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

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getTemplate(), getExecutable().orElse(null));
    }

    @Override
    public String toString() {
        Optional<Executable> executable = getExecutable();
        return executable.isPresent() ? ("{\"path\": \"" + getPath() + "\", \"js\": " + executable.get() + "}") :
                super.toString();
    }
}
