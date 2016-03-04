package org.wso2.carbon.uuf.core.util;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.TagType;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.HandlebarsRenderble;
import org.wso2.carbon.uuf.core.Renderble;
import org.wso2.carbon.uuf.core.UUFException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class InitHandlebarsUtil {
    private static final Handlebars HANDLEBARS = new Handlebars();
    private static final String ZONES_KEY = InitHandlebarsUtil.class.getName() + "#zones";
    private static final String LAYOUT_KEY = InitHandlebarsUtil.class.getName() + "#layout";

    static {

        HANDLEBARS.registerHelper("fillZone", (context, options) -> {
            Map<String, Renderble> zones = options.data(ZONES_KEY);
            if (!(context instanceof String)) {
                throw new UUFException("fillZone must have a string literal name", INTERNAL_SERVER_ERROR);
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
            TemplateSource source = new StringTemplateSource(options.fn.filename(), sb.toString());
            if (zones == null) {
                zones = new HashMap<>();
                options.data(ZONES_KEY, zones);
            }
            zones.put(zoneName, new HandlebarsRenderble(source));
            return "";
        });


        HANDLEBARS.registerHelper("layout", (layoutName, options) -> {
            Object originalLayout = options.data(LAYOUT_KEY);
            if (originalLayout != null) {
                throw new UUFException(
                        "multiple layout '" + layoutName + "','" + originalLayout + "'  defined",
                        INTERNAL_SERVER_ERROR);
            }
            options.data(LAYOUT_KEY, layoutName);
            return "";
        });

        HANDLEBARS.registerHelperMissing((context, options) -> {
            if (options.tagType == TagType.VAR) {
                return "";
            } else {
                throw new UUFException("unknown helper" + context, INTERNAL_SERVER_ERROR);
            }
        });
    }

    public static Template compile(TemplateSource source) {
        try {
            return HANDLEBARS.compile(source);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", e);
        }
    }

    public static Map<String, Renderble> getFillingZones(Context context) {
        return context.data(ZONES_KEY);
    }

    public static String getLayoutName(Context context) {
        return context.data(LAYOUT_KEY);
    }
}
