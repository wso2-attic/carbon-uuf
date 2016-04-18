package org.wso2.carbon.uuf.core.create;

import java.util.List;

public interface AppReference {

    String getName();

    ComponentReference getComponentReference(String name);

    List<String> getDependencies();
}
