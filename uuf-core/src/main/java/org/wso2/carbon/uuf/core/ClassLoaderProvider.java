package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.core.create.ComponentReference;

public interface ClassLoaderProvider {

    /**
     * Returns class loader of the given component.
     *
     * @param appName            application name
     * @param componentName      full component name
     * @param componentVersion   component version
     * @param componentReference component reference
     * @return class loader for specified component
     */
    ClassLoader getClassLoader(String appName, String componentName, String componentVersion,
                               ComponentReference componentReference);
}