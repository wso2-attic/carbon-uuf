package org.wso2.carbon.uuf.renderablecreator.hbs.impl;

import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.Executable;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.MutableHbsRenderable;

public class MutableLayoutRenderable extends HbsLayoutRenderable implements MutableHbsRenderable {

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

    @Override
    public void setTemplate(Template template) {
        this.template = template;
    }

    @Override
    public void setExecutable(Executable executable) {
        throw new UnsupportedOperationException("Layouts do not have executable.");
    }
}
