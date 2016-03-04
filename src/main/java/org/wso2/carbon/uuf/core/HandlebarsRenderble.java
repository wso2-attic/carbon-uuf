package org.wso2.carbon.uuf.core;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.util.InitHandlebarsUtil;
import org.wso2.carbon.uuf.core.util.RuntimeHandlebarsUtil;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;


public class HandlebarsRenderble implements Renderble {

    private final Template template;
    private final Map<String, Renderble> fillingZones;
    @Nullable
    private final String layoutName;


    public HandlebarsRenderble(TemplateSource source) {
        this.template = RuntimeHandlebarsUtil.compile(source);

        Context context = Context.newContext(Collections.EMPTY_MAP);
        try {
            Template preTemplate = InitHandlebarsUtil.compile(source);
            preTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
        this.layoutName = InitHandlebarsUtil.getLayoutName(context);
        this.fillingZones = InitHandlebarsUtil.getFillingZones(context);
    }


    @Override
    public String render(Object o, Map<String, Renderble> zones) {
        Context context = Context.newContext(o);
        context.data(RuntimeHandlebarsUtil.ZONES_KEY, zones);
        try {
            return template.apply(context);
        } catch (IOException e) {
            throw new UUFException("Handlebars rendering failed", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public Map<String, Renderble> getFillingZones() {
        return fillingZones;
    }

    @Override
    public String toString() {
        return "{path:'" + template.filename() + "'}";
    }

    @Override
    @Nullable
    public String getLayoutName() {
        return layoutName;
    }
}
