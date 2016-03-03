package org.wso2.carbon.uuf.core;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HandlebarsRenderble implements Renderble {

    private final Template template;

    private static final Logger log = LoggerFactory.getLogger(Page.class);

    private static final Handlebars handlebars = new Handlebars();

    static {
        handlebars.registerHelperMissing((context, options) -> {
            if (options.tagType == TagType.VAR) {
                throw new RuntimeException("value not available for the variable '"
                        + options.helperName + "' in " + context);
            } else {
                return "<missing>";
            }
        });
    }

    private static Template compile(TemplateSource source) {
        try {
            return handlebars.compile(source);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    public HandlebarsRenderble(String templateString, String name) {
        this.template = compile(new StringTemplateSource(name, templateString));
    }

    @Override
    public String render(Object o) {
        try {
            return template.apply(o);
        } catch (IOException e) {
            throw new UUFException("Handlebars rendering failed", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public String toString() {
        return "{path:'" + template.filename() + "'}";
    }
}
