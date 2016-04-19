package org.wso2.carbon.uuf.core.create;

public interface PageReference {

    String getPathPattern();

    FileReference getRenderingFile();

    @Deprecated
    AppReference getAppReference();

    @Deprecated
    ComponentReference getComponentReference();
}
