package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.io.TemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.core.exception.UUFException;
import org.wso2.carbon.uuf.handlebars.model.ContextModel;
import org.wso2.carbon.uuf.model.Model;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class HbsFragmentRenderable extends HbsRenderable {
    private static final Logger log = LoggerFactory.getLogger(HbsRenderable.class);

    private final Optional<Executable> executable;

    public HbsFragmentRenderable(TemplateSource template) {
        this(template, Optional.<Executable>empty());
    }

    public HbsFragmentRenderable(TemplateSource template, Executable executable) {
        this(template, Optional.of(executable));
    }

    private HbsFragmentRenderable(TemplateSource template, Optional<Executable> executable) {
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
        try {
            return compiledTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("An error occurred when writing to the in-memory PlaceholderWriter.", e);
        }
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + templatePath + "\"" +
                (executable.isPresent() ? ",\"js\": \"" + executable + "\"}" : "}");
    }
}
