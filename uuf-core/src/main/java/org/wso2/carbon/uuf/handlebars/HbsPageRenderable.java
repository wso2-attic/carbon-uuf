package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.helpers.init.FillZoneHelper;
import org.wso2.carbon.uuf.handlebars.helpers.init.LayoutHelper;
import org.wso2.carbon.uuf.handlebars.helpers.runtime.ResourceHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HbsPageRenderable extends HbsRenderable {
    private final Map<String, HbsPageRenderable> fillingZone;
    private final List<String> headJs;
    private final Optional<String> layout;

    private static final Handlebars HANDLEBARS = new Handlebars();

    static {
        HANDLEBARS.registerHelper("fillZone", FillZoneHelper.getInstance());
        HANDLEBARS.registerHelper("layout", LayoutHelper.getInstance());
        HANDLEBARS.registerHelper("headJs", ResourceHelper.JS_INSTANCE);
        HANDLEBARS.registerHelperMissing((context, options) -> "");
    }
    
    public HbsPageRenderable(TemplateSource template, Optional<Executable> executable) {
        super(template, executable);
        Template compiledTemplate;
        Context context = Context.newContext(Collections.EMPTY_MAP);
        try {
            compiledTemplate = HANDLEBARS.compile(template);
            compiledTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", e);
        }
        Map<String, HbsPageRenderable> zones = context.data(FillZoneHelper.ZONES_KEY);
        fillingZone = (zones == null) ? Collections.emptyMap() : zones;
        layout = Optional.ofNullable(context.data(LayoutHelper.LAYOUT_KEY));
        List<String> headJsList = context.data(ResourceHelper.JS_INSTANCE.getResourceKey());
        headJs = (headJsList == null) ? Collections.emptyList() : headJsList;
    }

    public List<String> getHeadJs() {
        return headJs;
    }

    public Map<String, HbsPageRenderable> getFillingZones() {
        return fillingZone;
    }

    public Optional<String> getLayoutName() {
        return layout;
    }
}
