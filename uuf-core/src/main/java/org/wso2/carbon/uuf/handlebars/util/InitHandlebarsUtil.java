package org.wso2.carbon.uuf.handlebars.util;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.HbsRenderable;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;


public class InitHandlebarsUtil {
    private static final Handlebars HANDLEBARS = new Handlebars();
    private static final String ZONES_KEY = InitHandlebarsUtil.class.getName() + "#zones";
    private static final String LAYOUT_KEY = InitHandlebarsUtil.class.getName() + "#layout";
    private static final String CSS_KEY = InitHandlebarsUtil.class.getName() + "#css";

    static {

        HANDLEBARS.registerHelper("fillZone", (context, options) -> {
            Map<String, Renderable> zones = options.data(ZONES_KEY);
            if (!(context instanceof String)) {
                throw new UUFException("fillZone must have a string literal name");
            }
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
            if (zones == null) {
                zones = new HashMap<>();
                options.data(ZONES_KEY, zones);
            }
            zones.put(zoneName, new HbsRenderable(sb.toString(), Paths.get(options.fn.filename())));
            return "";
        });


        HANDLEBARS.registerHelper("layout", (layoutName, options) -> {
            Object originalLayout = options.data(LAYOUT_KEY);
            if (originalLayout != null) {
                throw new UUFException(
                        "multiple layout '" + layoutName + "','" + originalLayout + "'  defined");
            }
            options.data(LAYOUT_KEY, layoutName);
            return "";
        });

        HANDLEBARS.registerHelper("headCss", (cssUri, options) -> {
            String cssUriString = cssUri.toString();
            if (cssUriString.startsWith("/")) {
                throw new IllegalArgumentException("URI fragment relative public URI should start with '/'");
            }
            List<String> cssMap = options.data(CSS_KEY);
            if (cssMap == null) {
                cssMap = new ArrayList<>();
                options.data(CSS_KEY, cssMap);
            }
            cssMap.add(cssUriString);
            return "";
        });

        HANDLEBARS.registerHelperMissing((context, options) -> "");
    }

    public static Template compile(TemplateSource source) {
        try {
            return HANDLEBARS.compile(source);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", e);
        }
    }

    public static Map<String, Renderable> getFillingZones(Context context) {
        Map<String, Renderable> zones = context.data(ZONES_KEY);
        return zones == null ? Collections.emptyMap() : zones;
    }

    public static Optional<String> getLayoutName(Context context) {
        return Optional.ofNullable(context.data(LAYOUT_KEY));
    }
}
