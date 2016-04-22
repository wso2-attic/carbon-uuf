package org.wso2.carbon.uuf.core.create;

import java.util.List;

public interface AppReference {

    String DIR_NAME_COMPONENTS = "components";
    String FILE_NAME_DEPENDENCY_TREE = "dependency.tree";

    String getName();

    ComponentReference getComponentReference(String componentSimpleName);

    List<String> getDependencies();
}
