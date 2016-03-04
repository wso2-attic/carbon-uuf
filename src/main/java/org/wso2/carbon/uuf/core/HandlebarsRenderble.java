package org.wso2.carbon.uuf.core;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HandlebarsRenderble implements Renderble {

    private static final Logger log = LoggerFactory.getLogger(HandlebarsRenderble.class);
    private static final String ZONES_KEY = HandlebarsRenderble.class.getName() + "#zones";
    private static final Handlebars HANDLEBARS = new Handlebars();

    private final Template template;

    static {
        HANDLEBARS.registerHelper("defineZone", (context, options) -> {
            Map<String, Renderble> zones = options.data(ZONES_KEY);
            String zoneName = (String) context;
            Renderble renderble = zones.get(zoneName);
            if (renderble != null) {
                //TODO: maybe use the same context
                return new Handlebars.SafeString(renderble.render(options.context.model(), zones).trim());
            }
            throw new UUFException("zone '" + zoneName + "' not available", Response.Status.INTERNAL_SERVER_ERROR);
        });

        HANDLEBARS.registerHelperMissing((context, options) -> {
            if (options.tagType == TagType.VAR) {
                throw new RuntimeException("value not available for the variable '"
                        + options.helperName + "' in " + context);
            } else {
                return "<missing>";
            }
        });

    }

    private final String templateString;
    private final String name;

    private static Template compile(TemplateSource source) {
        try {
            return HANDLEBARS.compile(source);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    public HandlebarsRenderble(String templateString, String name) {
        this.templateString = templateString;
        this.name = name;
        this.template = compile(new StringTemplateSource(name, templateString));
    }

    @Override
    public String render(Object o, Map<String, Renderble> zones) {
        Context context = Context.newContext(o);
        context.data(ZONES_KEY, zones);
        try {
            return template.apply(context);
        } catch (IOException e) {
            throw new UUFException("Handlebars rendering failed", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public Map<String, Renderble> getFillingZones() {
        Map<String, Renderble> map = new HashMap<>();
        Handlebars handlebars = new Handlebars();

        handlebars.registerHelper("fillZone", (context, options) -> {
            Map<String, Renderble> zones = options.data(ZONES_KEY);
            String zoneName = (String) context;

            //we will prepend with a space and enter string to make the line numbers correct
            int line = options.fn.position()[0];
            int col = options.fn.position()[1];
            String crlf = System.lineSeparator();
            StringBuilder sb = new StringBuilder(col + (line - 1) * crlf.length());
            for (int i = 0; i < line - 1; i++) {
                sb.append(crlf);
            }
            for (int i = 0; i < col; i++) {
                sb.append(' ');
            }

            sb.append(options.fn.text());
            zones.put(zoneName, new HandlebarsRenderble(sb.toString(), name + "#" + context));
            return "";
        });

        Context context = Context.newContext(Collections.EMPTY_MAP);
        context.data(ZONES_KEY, map);

        try {
            Template preTemplate = handlebars.compile(new StringTemplateSource(name, templateString));
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

}
