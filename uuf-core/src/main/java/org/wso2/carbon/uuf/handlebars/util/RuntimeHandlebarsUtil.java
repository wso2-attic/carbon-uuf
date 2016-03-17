package org.wso2.carbon.uuf.handlebars.util;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class RuntimeHandlebarsUtil {

    private static final ImmutableSet<String> KEYWORDS = ImmutableSet.of("layout", "fillZone", "headCss");
    private static final Handlebars HANDLEBARS = new Handlebars();
    private static final String BINDING_KEY = RuntimeHandlebarsUtil.class.getName() + "#bindings";
    private static final String FRAGMENT_KEY = RuntimeHandlebarsUtil.class.getName() + "#fragments";
    private static final String CONFIG_KEY = RuntimeHandlebarsUtil.class.getName() + "#config";


    static {
        HANDLEBARS.registerHelper("defineZone", (context, options) -> {
            //TODO: remove duplicate, includeFragment
            Multimap<String, Renderable> bindings = options.data(BINDING_KEY);
            Map<String, Fragment> fragments = options.data(FRAGMENT_KEY);
            String zoneName = (String) context;
            Collection<Renderable> renderables = bindings.get(zoneName);
            if (renderables.isEmpty()) {
                throw new UUFException("Zone '" + zoneName + "' does not have a binding.");
            }
            StringBuilder buffer = new StringBuilder();
            for (Renderable renderable : renderables) {
                //TODO: maybe use the same context
                String content = renderable.render(options.context, bindings, fragments).trim();
                buffer.append(content);
            }
            return new Handlebars.SafeString(buffer.toString());

        });

        HANDLEBARS.registerHelper("config", (configKey, options) -> {
            Map<String, String> configMap = options.data(CONFIG_KEY);
            String config = null;
            if (configMap != null) {
                String configKeyStr = (String) configKey;
                config = configMap.get(configKeyStr);
            }
            if (config != null) {
                return config;
            } else {
                return options.fn();
            }
        });


        HANDLEBARS.registerHelper("includeFragment", (fragmentName, options) -> {
            //TODO: remove duplicate, defineZone
            Multimap<String, Renderable> bindings = options.data(BINDING_KEY);
            Map<String, Fragment> fragments = options.data(FRAGMENT_KEY);
            String fragmentNameStr = (String) fragmentName;
            Fragment fragment = fragments.get(fragmentNameStr);
            if (fragment != null) {
                //TODO: maybe use the same context
                Map<String, Object> fragmentArgs = options.hash;
                Object fragmentContext;
                if (fragmentArgs.isEmpty()) {
                    fragmentContext = options.context;
                } else {
                    fragmentContext = fragmentArgs;
                }
                String content = fragment.render(fragmentContext, bindings, fragments).trim();
                return new Handlebars.SafeString(content);
            }
            throw new UUFException("fragment '" + fragmentNameStr + "' not available");
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

    public static void setBindings(Context context, Multimap<String, Renderable> bindings) {
        context.data(BINDING_KEY, bindings);
    }

    public static void setFragments(Context context, Map<String, Fragment> fragments) {
        context.data(FRAGMENT_KEY, fragments);
    }
}
