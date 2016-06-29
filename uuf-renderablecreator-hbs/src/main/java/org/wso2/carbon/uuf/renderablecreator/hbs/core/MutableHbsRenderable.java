package org.wso2.carbon.uuf.renderablecreator.hbs.core;

import com.github.jknack.handlebars.Template;

public interface MutableHbsRenderable {

    void setTemplate(Template template);

    void setExecutable(Executable executable);
}
