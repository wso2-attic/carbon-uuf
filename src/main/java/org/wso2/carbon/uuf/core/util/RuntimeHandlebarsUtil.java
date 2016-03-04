package org.wso2.carbon.uuf.core.util;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.Renderble;
import org.wso2.carbon.uuf.core.UUFException;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

public class RuntimeHandlebarsUtil {

    private static final Handlebars HANDLEBARS = new Handlebars();
    public static final String ZONES_KEY = RuntimeHandlebarsUtil.class.getName() + "#zones";

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

    public static Template compile(TemplateSource source) {
        try {
            return HANDLEBARS.compile(source);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }
}
