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
import java.util.Map;

public class HbsFragmentRenderable extends HbsPageRenderable {

    private static final Logger log = LoggerFactory.getLogger(HbsFragmentRenderable.class);

    public HbsFragmentRenderable(TemplateSource template) {
        super(template);
    }

    public HbsFragmentRenderable(TemplateSource template, Executable executable) {
        super(template, executable);
    }

    @Override
    public String render(Model model, ComponentLookup componentLookup, RequestLookup requestLookup, API api) {
        Context context;
        if (executable.isPresent()) {
            Object executableOutput = executeExecutable(getExecutableContext(model, requestLookup), api);
            if (log.isDebugEnabled()) {
                log.debug("Executable output \"" + DebugUtil.safeJsonString(executableOutput) + "\".");
            }
            if (model instanceof ContextModel) {
                context = Context.newContext(((ContextModel) model).getParentContext(), executableOutput);
            } else {
                context = Context.newContext(executableOutput);
            }
            context.combine(getHbsModel(model, requestLookup));
        } else {
            Map<String, Object> hbsModel = getHbsModel(model, requestLookup);
            if (model instanceof ContextModel) {
                context = Context.newContext(((ContextModel) model).getParentContext(), hbsModel);
            } else {
                context = Context.newContext(hbsModel);
            }
        }

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

    private Map<String, Object> getExecutableContext(Model model, RequestLookup requestLookup) {
        Map<String, Object> context = getExecutableContext(requestLookup);
        context.put("params", model.toMap());
        return context;
    }

    private Map<String, Object> getHbsModel(Model model, RequestLookup requestLookup) {
        Map<String, Object> context = getHbsModel(requestLookup);
        context.put("@params", model.toMap());
        return context;
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + templatePath + "\"" +
                (executable.isPresent() ? ",\"js\": \"" + executable + "\"}" : "}");
    }
}
