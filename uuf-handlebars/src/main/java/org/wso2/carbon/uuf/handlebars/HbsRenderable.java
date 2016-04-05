package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.CssHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.DefineZoneHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.IncludeFragmentHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.JsHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.MissingHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.PlaceholderHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.PublicHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.ResourceHelper;
import org.wso2.carbon.uuf.handlebars.model.ContextModel;
import org.wso2.carbon.uuf.model.Model;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HbsRenderable implements Renderable {

    public static final String FRAGMENTS_STACK_KEY =
            HbsRenderable.class.getName() + "#fragments-stack";
    public static final String LOOKUP_KEY = HbsRenderable.class.getName() + "#lookup";
    public static final String URI_KEY = HbsRenderable.class.getName() + "#uri";
    public static final String COMPONENT_NAME_KEY = HbsRenderable.class.getName() + "#comp-name";
    public static final String WRITER_KEY = HbsRenderable.class.getName() + "#writer";
    private static final Handlebars HANDLEBARS = new Handlebars();
    private static final Logger log = LoggerFactory.getLogger(HbsRenderable.class);
    private static final Map<String, ResourceHelper> RESOURCE_HELPERS = ImmutableMap.of(
            CssHelper.HELPER_NAME, new CssHelper(),
            JsHelper.HELPER_NAME_HEADER, new JsHelper(JsHelper.HELPER_NAME_HEADER),
            JsHelper.HELPER_NAME_FOOTER, new JsHelper(JsHelper.HELPER_NAME_FOOTER));

    static {
        HANDLEBARS.registerHelper(DefineZoneHelper.HELPER_NAME, new DefineZoneHelper());
        HANDLEBARS.registerHelper(IncludeFragmentHelper.HELPER_NAME, new IncludeFragmentHelper());
        HANDLEBARS.registerHelper(PlaceholderHelper.HELPER_NAME, new PlaceholderHelper());
        HANDLEBARS.registerHelper(PublicHelper.HELPER_NAME, new PublicHelper());
        RESOURCE_HELPERS.forEach(HANDLEBARS::registerHelper);
        HANDLEBARS.registerHelperMissing(new MissingHelper());
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
    public String render(String uriUpToContext, Model model, Lookup lookup) {
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
                throw new UnsupportedOperationException();
            }
        }
        ContextModel contextModel = ContextModel.from(model);
        Context context = contextModel.getContext();
        context.data(LOOKUP_KEY, lookup);
        context.data(URI_KEY, uriUpToContext);
        context.data(COMPONENT_NAME_KEY, lookup.getContext());
        if (log.isDebugEnabled()) {
            log.debug("Template " + this + " was applied with context " + DebugUtil.safeJsonString(context));
        }
        PlaceholderWriter writer = new PlaceholderWriter();
        context.data(WRITER_KEY, writer);
        try {
            compiledTemplate.apply(context, writer);
        } catch (IOException e) {
            throw new UUFException("Error while wringing to in-memory writer", e);
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
