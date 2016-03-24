package org.wso2.carbon.uuf.core.create;

import java.util.Optional;

public interface FileReference {

    String getName();

    String getPathPattern();

    String getContent();

    String getRelativePath();

    Optional<FileReference> getSiblingIfExists(String name);

}

