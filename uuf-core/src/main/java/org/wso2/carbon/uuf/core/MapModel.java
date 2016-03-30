package org.wso2.carbon.uuf.core;

import java.util.Map;

public class MapModel implements Model {
    protected Map<String, Object> map;

    public MapModel(Map<String, Object> map) {
        this.map = map;
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
