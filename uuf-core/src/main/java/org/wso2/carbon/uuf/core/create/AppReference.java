package org.wso2.carbon.uuf.core.create;

import java.util.List;
import java.util.stream.Stream;

public interface AppReference {

    Stream<ComponentReference> streamComponents();

    ComponentReference getComponentReference(String name);

    String getName();

    List<String> getDependencyTree();
}
