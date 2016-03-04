package org.wso2.carbon.uuf.core;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.util.InitHandlebarsUtil;
import org.wso2.carbon.uuf.core.util.RuntimeHandlebarsUtil;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class HandlebarsRenderble implements Renderble {

    private static final Logger log = LoggerFactory.getLogger(HandlebarsRenderble.class);

    private final Template template;
    private final TemplateSource source;


    public HandlebarsRenderble(TemplateSource source) {
        this.source = source;
        this.template = RuntimeHandlebarsUtil.compile(source);
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
        Map<String, Renderble> map = new HashMap<>();
        Context context = Context.newContext(Collections.EMPTY_MAP);
        context.data(InitHandlebarsUtil.ZONES_KEY, map);
        try {
            Template preTemplate = InitHandlebarsUtil.compile(source);
            preTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
        return map;
    }

    @Override
    public String toString() {
        return "{path:'" + template.filename() + "'}";
    }

    @Override
    @Nullable
    public String getLayoutName() {
        Context context = Context.newContext(Collections.EMPTY_MAP);
        try {
            Template preTemplate = InitHandlebarsUtil.compile(source);
            preTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
        return (String) context.data(InitHandlebarsUtil.LAYOUT_KEY);
    }
}
