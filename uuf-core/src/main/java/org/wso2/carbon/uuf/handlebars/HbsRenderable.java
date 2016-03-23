package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.DebugUtil;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.ConfigHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.DefineZoneHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.IncludeFragmentHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.MissingHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class HbsRenderable implements Renderable {

    public static final String BINDING_KEY = HbsRenderable.class.getName() + "#bindings";
    public static final String FRAGMENT_KEY = HbsRenderable.class.getName() + "#fragments";
    private static final Logger log = LoggerFactory.getLogger(HbsRenderable.class);
    private static final Handlebars HANDLEBARS = new Handlebars();

    static {
        HANDLEBARS.registerHelper("defineZone", DefineZoneHelper.INSTANCE);
        HANDLEBARS.registerHelper("config", ConfigHelper.INSTANCE);
        HANDLEBARS.registerHelper("includeFragment", IncludeFragmentHelper.INSTANCE);
        HANDLEBARS.registerHelperMissing(MissingHelper.INSTANCE);
    }

    private final Optional<Executable> executable;
    private final Template compiledTemplate;
    private final String templatePath;

    public HbsRenderable(TemplateSource template, Optional<Executable> executable) {
        this.executable = executable;
        this.templatePath = template.filename();
        try {
            this.compiledTemplate = HANDLEBARS.compile(template);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", e);
        }
    }

    public Optional<Executable> getScript() {
        return executable;
    }


    @Override
    public String render(Object model, Multimap<String, Renderable> bindings, Map<String, Fragment> fragments) {
        Context context = objectToContext(model);
        if (executable.isPresent()) {
            Object jsModel = executable.get().execute(Collections.EMPTY_MAP);
            if (log.isDebugEnabled()) {
                log.debug("js ran produced output " + DebugUtil.safeJsonString(jsModel));
            }
            if (jsModel instanceof Map) {
                //noinspection unchecked
                context.combine((Map<String, ?>) jsModel);
            } else {
                throw new UnsupportedOperationException();
            }
        }
        context.data(FRAGMENT_KEY, fragments);
        context.data(BINDING_KEY, bindings);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Template " + this + " was applied with context " + DebugUtil.safeJsonString(context));
            }
            return compiledTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("Handlebars rendering failed", e);
        }
    }

    private Context objectToContext(Object candidateContext) {
        if (candidateContext instanceof Context) {
            return (Context) candidateContext;
        } else {
            return Context.newContext(candidateContext);
        }
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + templatePath + "\"" +
                (executable.isPresent() ? ",\"js\": \"" + executable + "\"}" : "}");
    }
}
