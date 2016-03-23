package org.wso2.carbon.uuf.core;

import java.util.Optional;

public interface FileReference {

    String getName();

    String getPathRelativeToPagesRoot();

    String getContent();

    String getPathRelativeToApp();

    Optional<FileReference> getSibling(String name);

    FileReference getChild(String s);
}

