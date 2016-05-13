/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.uuf.api.model;

import org.wso2.carbon.uuf.spi.model.Model;

import java.util.Map;

/**
 * Implements the {@link Model} interface to provide a Map based model.
 */
public class MapModel implements Model {

    private Map<String, Object> map;

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
