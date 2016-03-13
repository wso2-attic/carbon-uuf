package org.wso2.carbon.uuf.core;


import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

public interface Renderable {


    String render(Object o, Map<String, Renderable> zones, Map<String, Renderable> fragments);

    default Map<String, Renderable> getFillingZones() {
        return Collections.emptyMap();
    }

    @Nullable
    default String getLayoutName() {
        return null;
    }
}
