package org.wso2.carbon.uuf.core.util;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import org.apache.commons.logging.impl.LogKitLogger;
import org.wso2.carbon.uuf.core.HandlebarsRenderble;
import org.wso2.carbon.uuf.core.Renderble;
import org.wso2.carbon.uuf.core.UUFException;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

public class InitHandlebarsUtil {
    private static final Handlebars HANDLEBARS = new Handlebars();
    public static final String ZONES_KEY = InitHandlebarsUtil.class.getName() + "#zones";
    public static final String LAYOUT_KEY = InitHandlebarsUtil.class.getName() + "#layout";

    static {

        HANDLEBARS.registerHelper("fillZone", (context, options) -> {
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
            TemplateSource source = new StringTemplateSource(options.fn.filename(), sb.toString());
            zones.put(zoneName, new HandlebarsRenderble(source));
            return "";
        });


        HANDLEBARS.registerHelper("layout", (layoutName, options) -> {
            Object originalLayout = options.data(LAYOUT_KEY);
            if (originalLayout != null) {
                throw new UUFException(
                        "multiple layout '" + layoutName + "','" + originalLayout + "'  defined",
                        Response.Status.INTERNAL_SERVER_ERROR);
            }
            options.data(LAYOUT_KEY, layoutName);
            return "";
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
