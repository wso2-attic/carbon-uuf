package org.wso2.carbon.uuf.core.create;

import java.util.stream.Stream;

public interface FragmentReference {

    String getName();

    FileReference getChild(String name);

    Stream<FileReference> streamChildren();
}
