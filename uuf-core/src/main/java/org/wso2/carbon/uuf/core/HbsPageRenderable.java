package org.wso2.carbon.uuf.core;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.util.InitHandlebarsUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class HbsPageRenderable extends HandlebarsRenderable {
    private final Optional<String> layoutName;
    private final Map<String, Renderable> fillingZones;

    public HbsPageRenderable(TemplateSource source) {
        super(source);

        Context context = Context.newContext(Collections.EMPTY_MAP);
        try {
            Template preTemplate = InitHandlebarsUtil.compile(source);
            preTemplate.apply(context);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", e);
        }
        this.layoutName = InitHandlebarsUtil.getLayoutName(context);
        this.fillingZones = InitHandlebarsUtil.getFillingZones(context);
    }

    public Optional<String> getLayoutName() {
        return layoutName;
    }

    public Map<String, Renderable> getFillingZones() {
        return fillingZones;
    }
}
