package org.wso2.carbon.uuf.renderablecreator.hbs.renderable;

import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;

public class MutableLayoutRenderable extends HbsLayoutRenderable {

    private volatile Template template;
    private final String path;

    public MutableLayoutRenderable(TemplateSource templateSource) {
        this.template = compile(templateSource);
        this.path = templateSource.filename();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }
}
