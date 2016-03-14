package org.wso2.carbon.uuf.core;


import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public interface Renderable {


    String render(Object o, Map<String, Renderable> zones, Map<String, Renderable> fragments);

    default Map<String, Renderable> getFillingZones() {
        return Collections.emptyMap();
    }

    default Optional<String> getLayoutName() {
        return null;
    }
}
