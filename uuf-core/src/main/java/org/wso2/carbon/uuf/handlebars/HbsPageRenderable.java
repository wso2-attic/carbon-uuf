package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.Context;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.util.InitHandlebarsUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class HbsPageRenderable extends HbsRenderable {
    private Optional<String> layoutName;
    private Map<String, Renderable> fillingZones;

    public HbsPageRenderable(String templateSource) {
        super(templateSource);
        initialParse();
    }

    public HbsPageRenderable(String templateSource, Path templatePath) {
        super(templateSource, templatePath);
        initialParse();
    }


    public HbsPageRenderable(String templateSource, Path templatePath, String scriptSource, Path scriptPath) {
        super(templateSource, templatePath, scriptSource, scriptPath);
        initialParse();
    }

    private void initialParse() {
        Context emptyContext = Context.newContext(new Object());
        try {
            InitHandlebarsUtil.compile(this.getTemplate()).apply(emptyContext);
        } catch (IOException e) {
            throw new UUFException("pages template completions error", e);
        }
        this.layoutName = InitHandlebarsUtil.getLayoutName(emptyContext);
        this.fillingZones = InitHandlebarsUtil.getFillingZones(emptyContext);
    }

    public Optional<String> getLayoutName() {
        return layoutName;
    }

    public Map<String, Renderable> getFillingZones() {
        return fillingZones;
    }
}
