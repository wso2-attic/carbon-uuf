package org.wso2.carbon.uuf.core;

import java.util.stream.Stream;

public interface ComponentReference {
    Stream<FileReference> streamPageFiles();

    Stream<FileReference> streamFragmentFiles();

    FileReference resolveLayout(String layoutName);

    String getName();

}

