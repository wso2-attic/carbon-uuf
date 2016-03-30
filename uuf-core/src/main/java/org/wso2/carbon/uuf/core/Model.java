package org.wso2.carbon.uuf.core;

import java.util.Map;

public interface Model {
    void combine(Map<String, Object> other);

    Map<String, Object> toMap();
}
