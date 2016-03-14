package org.wso2.carbon.uuf.core;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.Multimap;
import org.wso2.carbon.uuf.core.util.RuntimeHandlebarsUtil;

import java.io.IOException;
import java.util.Map;

public class HbsRenderable implements Renderable {

    private final String fileName;
    private final Template template;

    public HbsRenderable(TemplateSource source) {
        this.template = RuntimeHandlebarsUtil.compile(source);
        // We have to separately remember this since there is a bug in Handlebar lib's
        // filename() method when the file is empty or stats with a comment
        this.fileName = source.filename();

    }

    @Override
    public String render(Object model, Multimap<String, Renderable> bindings, Map<String, Fragment> fragments) {
        Context context = Context.newContext(model);
        RuntimeHandlebarsUtil.setBindings(context, bindings);
        RuntimeHandlebarsUtil.setFragments(context, fragments);
        try {
            return template.apply(context);
        } catch (IOException e) {
            throw new UUFException("Handlebars rendering failed", e);
        }
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + fileName + "\"}";
    }
}
