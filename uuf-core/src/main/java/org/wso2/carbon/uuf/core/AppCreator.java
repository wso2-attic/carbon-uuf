package org.wso2.carbon.uuf.core;

import java.nio.file.Path;

public interface AppCreator {

    String STATIC_RESOURCE_PREFIX="/public";
    String STATIC_RESOURCE_PATH_PARAM_ROOT="root";
    String STATIC_RESOURCE_PATH_PARAM_BASE="base";

    App createApp(String name, String context);

    Path resolve(String appName, String resourcePath);
}
