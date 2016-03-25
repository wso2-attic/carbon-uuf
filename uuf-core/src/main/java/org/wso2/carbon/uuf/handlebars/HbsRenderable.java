package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.DebugUtil;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.CssHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.DefineZoneHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.IncludeFragmentHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.JsHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.MissingHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.PlaceholderHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.ResourceHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HbsRenderable implements Renderable {

    public static final String BINDING_KEY = HbsRenderable.class.getName() + "#bindings";
    public static final String FRAGMENT_KEY = HbsRenderable.class.getName() + "#fragments";
    public static final String WRITER_KEY = HbsRenderable.class.getName() + "#writer";
    //
    private static final String HELPER_NAME_DEFINE_ZONE = "defineZone";
    private static final String HELPER_NAME_FRAGMENT = "includeFragment";
    private static final String HELPER_NAME_PLACEHOLDER = "placeholder";
    private static final String HELPER_NAME_HEADER_TITLE = "headerTitle";
    private static final String HELPER_NAME_HEADER_CSS = "headerCss";
    private static final String HELPER_NAME_HEADER_JS = "headerJs";
    private static final String HELPER_NAME_HEADER_OTHER = "headerOther";
    private static final String HELPER_NAME_FOOTER_JS = "footerJs";
    //
    private static final Handlebars HANDLEBARS = new Handlebars();
    private static final Map<String, ResourceHelper> RESOURCE_HELPERS;
    private static final Logger log = LoggerFactory.getLogger(HbsRenderable.class);

    static {
        HANDLEBARS.registerHelper(HELPER_NAME_DEFINE_ZONE, new DefineZoneHelper());
        HANDLEBARS.registerHelper(HELPER_NAME_FRAGMENT, new IncludeFragmentHelper());
        HANDLEBARS.registerHelper(HELPER_NAME_PLACEHOLDER, new PlaceholderHelper());
        RESOURCE_HELPERS = ImmutableMap.of(HELPER_NAME_HEADER_CSS, new CssHelper(HELPER_NAME_HEADER_CSS),
                                           HELPER_NAME_HEADER_JS, new JsHelper(HELPER_NAME_HEADER_JS),
                                           HELPER_NAME_FOOTER_JS, new JsHelper(HELPER_NAME_FOOTER_JS));
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
    public String render(Object model, Multimap<String, Renderable> bindings, Map<String, Fragment> fragments) {
        Context context = objectToContext(model);
        if (executable.isPresent()) {
            //TODO: set context for executable
            Object jsModel = executable.get().execute(Collections.emptyMap());
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
        if (log.isDebugEnabled()) {
            log.debug("Template " + this + " was applied with context " + DebugUtil.safeJsonString(context));
        }
        PlaceholderWriter writer = new PlaceholderWriter();
        context.data(WRITER_KEY, writer);
        try {
            compiledTemplate.apply(context, writer);
        } catch (IOException e) {
            throw new UUFException("Handlebars rendering failed", e);
        }

        Map<String, String> placeholderValuesMap = new HashMap<>();
        for (Map.Entry<String, ResourceHelper> entry : RESOURCE_HELPERS.entrySet()) {
            Optional<String> placeholderValue = entry.getValue().getResources(context);
            if (placeholderValue.isPresent()) {
                placeholderValuesMap.put(entry.getKey(), placeholderValue.get());
            }
        }
        return writer.toString(placeholderValuesMap);
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
