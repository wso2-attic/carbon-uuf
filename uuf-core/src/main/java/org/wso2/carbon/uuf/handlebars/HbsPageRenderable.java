package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.helpers.FillZoneInitHelper;
import org.wso2.carbon.uuf.handlebars.helpers.LayoutInitHelper;
import org.wso2.carbon.uuf.handlebars.helpers.ResourceInitHelper;

import java.io.IOException;
import java.util.*;

public class HbsPageRenderable extends HbsRenderable {
    private final Map<String, Renderable> fillingZone;
    private final List<String> headJs;
    private final Optional<String> layout;

    private static final Handlebars HANDLEBARS = new Handlebars();

    static {
        HANDLEBARS.registerHelper("fillZone", FillZoneInitHelper.INSTANCE);
        HANDLEBARS.registerHelper("layout", LayoutInitHelper.INSTANCE);
        HANDLEBARS.registerHelper("headJs", ResourceInitHelper.JS_INSTANCE);
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
        Map<String, Renderable> zones = context.data(FillZoneInitHelper.ZONES_KEY);
        fillingZone = (zones == null) ? Collections.emptyMap() : zones;
        layout = Optional.ofNullable(context.data(LayoutInitHelper.LAYOUT_KEY));
        List<String> headJsList = context.data(ResourceInitHelper.JS_INSTANCE.getResourceKey());
        headJs = (headJsList == null) ? Collections.emptyList() : headJsList;
    }

    public List<String> getHeadJs() {
        return headJs;
    }

    public Map<String, Renderable> getFillingZones() {
        return fillingZone;
    }

    public Optional<String> getLayoutName() {
        return layout;
    }
}
