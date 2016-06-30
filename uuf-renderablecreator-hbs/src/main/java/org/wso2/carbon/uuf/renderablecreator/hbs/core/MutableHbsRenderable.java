package org.wso2.carbon.uuf.renderablecreator.hbs.core;

import com.github.jknack.handlebars.io.TemplateSource;

public interface MutableHbsRenderable {

    String getPath();

    void setTemplateSource(TemplateSource templateSource);

    void setExecutable(Executable executable);
}
