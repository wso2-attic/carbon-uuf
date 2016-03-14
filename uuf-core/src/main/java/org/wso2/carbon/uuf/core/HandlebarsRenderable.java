package org.wso2.carbon.uuf.core;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.util.InitHandlebarsUtil;
import org.wso2.carbon.uuf.core.util.RuntimeHandlebarsUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;


public class HandlebarsRenderable implements Renderable {

    private final Template template;
    private final Map<String, Renderable> fillingZones;
    private final Optional<String> layoutName;
    private final String name;


    public HandlebarsRenderable(TemplateSource source) {
        this.template = RuntimeHandlebarsUtil.compile(source);
        // We have to separately remember this since there is a bug in Handlebar lib's
        // filename() method when the file is empty or stats with a comment
        this.name = source.filename();

        Context context = Context.newContext(Collections.EMPTY_MAP);
        try {
            Template preTemplate = InitHandlebarsUtil.compile(source);
            preTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", e);
        }
        this.layoutName = InitHandlebarsUtil.getLayoutName(context);
        this.fillingZones = InitHandlebarsUtil.getFillingZones(context);
    }


    @Override
    public String render(Object o, Map<String, Renderable> zones, Map<String, Renderable> fragments) {
        Context context = Context.newContext(o);
        RuntimeHandlebarsUtil.setZones(context, zones);
        RuntimeHandlebarsUtil.setFragment(context, fragments);
        try {
            return template.apply(context);
        } catch (IOException e) {
            throw new UUFException("Handlebars rendering failed", e);
        }
    }

    @Override
    public Map<String, Renderable> getFillingZones() {
        return fillingZones;
    }

    @Override
    public String toString() {
        return "{path:'" + name + "'}";
    }

    @Override
    public Optional<String> getLayoutName() {
        return layoutName;
    }
}
