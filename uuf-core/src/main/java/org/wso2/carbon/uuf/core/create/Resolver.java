package org.wso2.carbon.uuf.core.create;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface Resolver {

    String STATIC_RESOURCE_URI_PREFIX = "public";
    String STATIC_RESOURCE_URI_BASE_PREFIX = "base";

    Path resolveStatic(String appName, String resourcePath);

    Stream<ComponentReference> resolveComponents(String appName);

    ComponentReference resolveComponent(String appName, String name);

}
