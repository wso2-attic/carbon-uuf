package org.wso2.carbon.uuf.core;

import java.util.Collections;
import java.util.Map;

public class MapModel implements Model {
    protected Map<String, Object> map;

    public MapModel(Map<String, Object> map) {
        this.map = map;
    }

    public MapModel() {
        this(Collections.emptyMap());
    }

    @Override
    public void combine(Map<String, Object> other) {
        map.putAll(other);
    }

    @Override
    public Map<String, Object> toMap() {
        return map;
    }

}
