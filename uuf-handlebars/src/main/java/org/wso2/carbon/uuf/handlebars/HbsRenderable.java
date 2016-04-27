package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.core.exception.UUFException;
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
import java.util.Map;
import java.util.Optional;

public class HbsRenderable implements Renderable {

    public static final String DATA_KEY_LOOKUP = HbsRenderable.class.getName() + "#lookup";
    public static final String DATA_KEY_REQUEST_LOOKUP = HbsRenderable.class.getName() + "#request-lookup";
    public static final String DATA_KEY_API = HbsRenderable.class.getName() + "#api";
    //
    private static final Handlebars HANDLEBARS = new Handlebars();
    private static final Map<String, ResourceHelper> RESOURCE_HELPERS = ImmutableMap.of(
            CssHelper.HELPER_NAME, new CssHelper(),
            JsHelper.HELPER_NAME_HEADER, new JsHelper(JsHelper.HELPER_NAME_HEADER),
            JsHelper.HELPER_NAME_FOOTER, new JsHelper(JsHelper.HELPER_NAME_FOOTER));
    private static final Logger log = LoggerFactory.getLogger(HbsRenderable.class);

    static {
        HANDLEBARS.registerHelper(DefineZoneHelper.HELPER_NAME, new DefineZoneHelper());
        HANDLEBARS.registerHelper(IncludeFragmentHelper.HELPER_NAME, new IncludeFragmentHelper());
        HANDLEBARS.registerHelper(PlaceholderHelper.HELPER_NAME, new PlaceholderHelper());
        HANDLEBARS.registerHelper(PublicHelper.HELPER_NAME, new PublicHelper());
        RESOURCE_HELPERS.forEach(HANDLEBARS::registerHelper);
        HANDLEBARS.registerHelperMissing(new MissingHelper());
    }

    protected final Template compiledTemplate;
    protected final String templatePath;
    private final Optional<Executable> executable;

    public HbsRenderable(TemplateSource template) {
        this(template, Optional.<Executable>empty());
    }

    public HbsRenderable(TemplateSource template, Executable executable) {
        this(template, Optional.of(executable));
    }

    public HbsRenderable(TemplateSource template, Optional<Executable> executable) {
        this.executable = executable;
        this.templatePath = template.filename();
        try {
            this.compiledTemplate = HANDLEBARS.compile(template);
        } catch (IOException e) {
            throw new UUFException("Cannot compile Handlebars template '" + templatePath + "'.", e);
        }
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
            log.debug("Template \"" + this + "\" was applied with context " + DebugUtil.safeJsonString(context));
        }
        try {
            return compiledTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException(
                    "An error occurred when rendering the compiled Handlebars template '" + templatePath + "'.", e);
        }
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + templatePath + "\"" + (executable.isPresent() ? ", \"js\": " + executable + "}" : "}");
    }
}
