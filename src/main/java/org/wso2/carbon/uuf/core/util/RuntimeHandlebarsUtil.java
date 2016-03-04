package org.wso2.carbon.uuf.core.util;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableSet;
import org.wso2.carbon.uuf.core.Renderble;
import org.wso2.carbon.uuf.core.UUFException;

import java.io.IOException;
import java.util.Map;

public class RuntimeHandlebarsUtil {

    private static final ImmutableSet<String> KEYWORDS = ImmutableSet.of("layout", "fillZone");
    private static final Handlebars HANDLEBARS = new Handlebars();
    private static final String ZONES_KEY = RuntimeHandlebarsUtil.class.getName() + "#zones";

    static {
        HANDLEBARS.registerHelper("defineZone", (context, options) -> {
            Map<String, Renderble> zones = options.data(ZONES_KEY);
            String zoneName = (String) context;
            Renderble renderble = zones.get(zoneName);
            if (renderble != null) {
                //TODO: maybe use the same context
                String content = renderble.render(options.context.model(), zones).trim();
                return new Handlebars.SafeString(content);
            }
            throw new UUFException("zone '" + zoneName + "' not available");
        });

        HANDLEBARS.registerHelperMissing((context, options) -> {
            if (KEYWORDS.contains(options.helperName)) {
                return "";
            }
            throw new UUFException(
                    "value not available for the variable '" +
                            options.helperName + "' in " + context);
        });

    }

    public static Template compile(TemplateSource source) {
        try {
            return HANDLEBARS.compile(source);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", e);
        }
    }

    public static void setZones(Context context, Map<String, Renderble> zones) {
        context.data(ZONES_KEY, zones);
    }
}
