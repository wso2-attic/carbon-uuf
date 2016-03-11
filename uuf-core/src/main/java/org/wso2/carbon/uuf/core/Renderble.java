package org.wso2.carbon.uuf.core;


import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

public interface Renderble {


    String render(Object o, Map<String, Renderble> zones, Map<String, Renderble> fragments);

    default Map<String, Renderble> getFillingZones() {
        return Collections.emptyMap();
    }

    @Nullable
    default String getLayoutName() {
        return null;
    }
}
