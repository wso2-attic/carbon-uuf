package org.wso2.carbon.uuf.renderablecreator.hbs.impl;

import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.MutableExecutable;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.MutableHbsRenderable;

import java.util.Optional;

public class MutableLayoutRenderable extends HbsLayoutRenderable implements MutableHbsRenderable {

    private final String path;
    private volatile Template template;

    public MutableLayoutRenderable(TemplateSource templateSource) {
        this.template = compile(templateSource);
        this.path = templateSource.filename();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    protected Template getTemplate() {
        return template;
    }

    @Override
    public void setTemplateSource(TemplateSource templateSource) {
        template = compile(templateSource);
    }

    @Override
    public Optional<MutableExecutable> getMutableExecutable() {
        throw new UnsupportedOperationException("Layouts do not have executable.");
    }
}
