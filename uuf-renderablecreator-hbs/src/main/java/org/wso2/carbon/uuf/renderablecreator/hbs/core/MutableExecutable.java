package org.wso2.carbon.uuf.renderablecreator.hbs.core;

public interface MutableExecutable extends Executable {

    void reload(String scriptSource);
}
