package org.wso2.carbon.uuf.renderablecreator.hbs.core;

import com.github.jknack.handlebars.io.TemplateSource;

import java.util.Optional;

public interface MutableHbsRenderable {

    String getPath();

    void setTemplateSource(TemplateSource templateSource);

    Optional<MutableExecutable> getMutableExecutable();
}
