package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.core.exception.UUFException;
import org.wso2.carbon.uuf.model.Model;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HbsPageRenderable extends HbsRenderable {

    private static final Logger log = LoggerFactory.getLogger(HbsPageRenderable.class);

    protected final Optional<Executable> executable;

    public HbsPageRenderable(TemplateSource template) {
        this(template, Optional.<Executable>empty());
    }

    public HbsPageRenderable(TemplateSource template, Executable executable) {
        this(template, Optional.of(executable));
    }

    private HbsPageRenderable(TemplateSource template, Optional<Executable> executable) {
        super(template);
        this.executable = executable;
    }

    @Override
    public String render(Model model, ComponentLookup componentLookup, RequestLookup requestLookup, API api) {
        Context context;
        if (executable.isPresent()) {
            Object executableOutput = executable.get().execute(getExecutableContext(requestLookup), api);
            if (log.isDebugEnabled()) {
                log.debug("Executable output \"" + DebugUtil.safeJsonString(executableOutput) + "\".");
            }
            context = Context.newContext(executableOutput).combine(getHbsModel(requestLookup));
        } else {
            context = Context.newContext(getHbsModel(requestLookup));
        }

        context.data(DATA_KEY_LOOKUP, componentLookup);
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

    protected Map<String, Object> getExecutableContext(RequestLookup requestLookup) {
        Map<String, Object> context = new HashMap<>();
        context.put("request", requestLookup.getRequest());
        context.put("uriParams", requestLookup);
        context.put("app", ImmutableMap.of("context", requestLookup.getAppContext(), "config", Collections.emptyMap()));
        return context;
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + templatePath + "\"" +
                (executable.isPresent() ? ",\"js\": \"" + executable + "\"}" : "}");
    }
}
