package org.wso2.carbon.uuf.core.create;

import java.util.Optional;

public interface FileReference {

    String getName();

    String getExtension();

    String getContent();

    String getRelativePath();

    Optional<FileReference> getSibling(String name);

    @Deprecated
    ComponentReference getComponentReference();

    @Deprecated
    AppReference getAppReference();
}
