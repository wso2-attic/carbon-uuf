package org.wso2.carbon.uuf.core;

import java.nio.file.Path;

public interface AppCreator {

    String STATIC_RESOURCE_URI_PREFIX ="public";
    String STATIC_RESOURCE_URI_BASE_PREFIX ="base";

    App createApp(String name, String context);

    Path resolve(String appName, String resourcePath);
}
