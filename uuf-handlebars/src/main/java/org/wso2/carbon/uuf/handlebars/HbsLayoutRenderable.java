package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.core.exception.UUFException;
import org.wso2.carbon.uuf.model.Model;

import java.io.IOException;

public class HbsLayoutRenderable extends HbsRenderable {

    public HbsLayoutRenderable(TemplateSource template) {
        super(template);
    }

    @Override
    public String render(Model model, ComponentLookup componentLookup, RequestLookup requestLookup, API api) {
        Context context = Context.newContext(new Object());
        context.data(DATA_KEY_LOOKUP, componentLookup);
        context.data(DATA_KEY_REQUEST_LOOKUP, requestLookup);
        context.data(DATA_KEY_API, api);

        try {
            return compiledTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("An error occurred when rendering the compiled Handlebars template of layout '" +
                                           templatePath + "'.", e);
        }
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + templatePath + "\"}";
    }
}
