package org.wso2.carbon.uuf.core.util;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableSet;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;

import java.io.IOException;
import java.util.Map;

public class RuntimeHandlebarsUtil {

    private static final ImmutableSet<String> KEYWORDS = ImmutableSet.of("layout", "fillZone");
    private static final Handlebars HANDLEBARS = new Handlebars();
    private static final String ZONES_KEY = RuntimeHandlebarsUtil.class.getName() + "#zones";
    private static final String FRAGMENT_KEY = RuntimeHandlebarsUtil.class.getName() + "#fragments";


    static {
        HANDLEBARS.registerHelper("defineZone", (context, options) -> {
            //TODO: remove duplicate, includeFragment
            Map<String, Renderable> zones = options.data(ZONES_KEY);
            Map<String, Renderable> fragments = options.data(FRAGMENT_KEY);
            String zoneName = (String) context;
            Renderable renderable = zones.get(zoneName);
            if (renderable != null) {
                //TODO: maybe use the same context
                String content = renderable.render(options.context.model(), zones, fragments).trim();
                return new Handlebars.SafeString(content);
            }
            throw new UUFException("zone '" + zoneName + "' not available");
        });

        HANDLEBARS.registerHelper("includeFragment", (context, options) -> {
            //TODO: remove duplicate, defineZone
            Map<String, Renderable> zones = options.data(ZONES_KEY);
            Map<String, Renderable> fragments = options.data(FRAGMENT_KEY);
            String fragmentName = (String) context;
            Renderable renderable = fragments.get(fragmentName);
            if (renderable != null) {
                //TODO: maybe use the same context
                String content = renderable.render(options.context.model(), zones, fragments).trim();
                return new Handlebars.SafeString(content);
            }
            throw new UUFException("fragment '" + fragmentName + "' not available");
        });

        HANDLEBARS.registerHelperMissing((context, options) -> {
            if (KEYWORDS.contains(options.helperName)) {
                return "";
            }
            throw new UUFException(
                    "value not available for the variable/helper '" +
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

    public static void setZones(Context context, Map<String, Renderable> zones) {
        context.data(ZONES_KEY, zones);
    }

    public static void setFragment(Context context, Map<String, Renderable> fragments) {
        context.data(FRAGMENT_KEY, fragments);
    }
}
