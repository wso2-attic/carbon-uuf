package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.io.TemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.core.exception.UUFException;
import org.wso2.carbon.uuf.handlebars.helpers.ResourceHelper;
import org.wso2.carbon.uuf.handlebars.model.ContextModel;
import org.wso2.carbon.uuf.model.Model;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class HbsPageRenderable extends HbsRenderable {

    private static final Logger log = LoggerFactory.getLogger(HbsRenderable.class);

    private final Optional<Executable> executable;

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

    public Optional<Executable> getScript() {
        return executable;
    }

    @Override
    public String render(Model model, ComponentLookup componentLookup, RequestLookup requestLookup, API api) {
        if (executable.isPresent()) {
            //TODO: set context for executable
            Object jsOutput = executable.get().execute(Collections.emptyMap());
            if (log.isDebugEnabled()) {
                log.debug("js ran produced output " + DebugUtil.safeJsonString(jsOutput));
            }
            if (jsOutput instanceof Map) {
                //noinspection unchecked
                model.combine((Map<String, Object>) jsOutput);
            } else {
                //TODO: is this necessary?
                throw new UnsupportedOperationException();
            }
        }
        ContextModel contextModel = ContextModel.from(model);
        Context context = contextModel.getContext();

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
        String out = writer.toString(getPlaceholderValues(context));
        writer.close();
        return out;
    }

    private Map<String, String> getPlaceholderValues(Context context) {
        Map<String, String> placeholderValuesMap = new HashMap<>();
        for (Map.Entry<String, ResourceHelper> entry : RESOURCE_HELPERS.entrySet()) {
            Optional<String> placeholderValue = entry.getValue().getPlaceholderValue(context);
            if (placeholderValue.isPresent()) {
                placeholderValuesMap.put(entry.getKey(), placeholderValue.get());
            }
        }
        return placeholderValuesMap;
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + templatePath + "\"" +
                (executable.isPresent() ? ",\"js\": \"" + executable + "\"}" : "}");
    }
}
