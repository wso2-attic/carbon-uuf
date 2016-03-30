package org.wso2.carbon.uuf.model;

import java.util.Map;

public interface Model {
    void combine(Map<String, Object> other);

    Map<String, Object> toMap();
}
