package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.Multimap;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.util.RuntimeHandlebarsUtil;

import java.io.IOException;
import java.util.Map;

public class HbsZoneRenderable implements Renderable {

    private final String zoneName;
    private final TemplateSource template;

    public HbsZoneRenderable(String zoneName, String templateSource, String templatePath) {
        this.zoneName = zoneName;
        this.template = new StringTemplateSource(templatePath, templateSource);
    }

    @Override
    public String render(Object model, Multimap<String, Renderable> bindings,
                         Map<String, Fragment> fragments) {
        Context context = Context.newContext(model);
        RuntimeHandlebarsUtil.setBindings(context, bindings);
        RuntimeHandlebarsUtil.setFragments(context, fragments);
        try {
            Template compiledTemplate = RuntimeHandlebarsUtil.compile(template);
            return compiledTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("Handlebars rendering failed", e);
        }
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + zoneName + "\"}";
    }
}
