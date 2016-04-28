package org.wso2.carbon.uuf.core.create;

import java.util.Optional;

public interface FileReference {

    String getName();

    String getExtension();

    String getContent();

    String getRelativePath();

    String getAbsolutePath();

    Optional<FileReference> getSibling(String name);
}
