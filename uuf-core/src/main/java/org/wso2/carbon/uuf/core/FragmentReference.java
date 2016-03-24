package org.wso2.carbon.uuf.core;

public interface FragmentReference {

    String getName();

    FileReference getChild(String name);
}
