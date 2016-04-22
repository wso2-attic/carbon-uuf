package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.helpers.init.FillZoneHelper;
import org.wso2.carbon.uuf.handlebars.helpers.init.LayoutHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class HbsInitRenderable extends HbsRenderable {

    public static final String DATA_KEY_ZONES = HbsInitRenderable.class.getName() + "#zones";
    public static final String DATA_KEY_CURRENT_LAYOUT = HbsInitRenderable.class.getName() + "#layout";
    private static final Handlebars HANDLEBARS = new Handlebars();

    static {
        HANDLEBARS.registerHelper(FillZoneHelper.HELPER_NAME, new FillZoneHelper());
        HANDLEBARS.registerHelper(LayoutHelper.HELPER_NAME, new LayoutHelper());
        HANDLEBARS.registerHelperMissing((context, options) -> "");
    }

    private final Map<String, HbsInitRenderable> fillingZone;
    private final Optional<String> layout;

    public HbsInitRenderable(TemplateSource template, Optional<Executable> executable) {
        super(template, executable);
        Template compiledTemplate;
        Context context = Context.newContext(Collections.emptyMap());
        try {
            compiledTemplate = HANDLEBARS.compile(template);
            compiledTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", e);
        }
        Map<String, HbsInitRenderable> zones = context.data(DATA_KEY_ZONES);
        fillingZone = (zones == null) ? Collections.emptyMap() : zones;
        layout = Optional.ofNullable(context.data(DATA_KEY_CURRENT_LAYOUT));
    }

    public Map<String, HbsInitRenderable> getFillingZones() {
        return fillingZone;
    }

    public Optional<String> getLayoutName() {
        return layout;
    }
}
