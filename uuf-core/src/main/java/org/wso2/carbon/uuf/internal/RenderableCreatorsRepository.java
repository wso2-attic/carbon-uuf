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

package org.wso2.carbon.uuf.internal;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.create.RenderableCreator;

import java.util.HashMap;
import java.util.Map;

public class RenderableCreatorsRepository {

    private static final Logger log = LoggerFactory.getLogger(RenderableCreatorsRepository.class);
    private static final RenderableCreatorsRepository instance = new RenderableCreatorsRepository();
    private Map<String, RenderableCreator> creators = new HashMap<>();

    private RenderableCreatorsRepository() {
    }

    /**
     * Always returns the same RenderableCreatorsRepository instance.
     *
     * @return the singleton RenderableCreatorsRepository instance
     */
    public static RenderableCreatorsRepository getInstance() {
        return instance;
    }

    public void add(RenderableCreator creator){
        creator.getSupportedFileExtensions().stream()
                .forEach(key -> creators.put(key, creator));
    }

    public void remove(RenderableCreator creator) {
        creator.getSupportedFileExtensions().stream()
                .forEach(key -> creators.remove(key));
    }

    public RenderableCreator get(String extension){
        return creators.get(extension);
    }

    public Map<String, RenderableCreator> getCreators(){
        return new ImmutableMap.Builder<String, RenderableCreator>().putAll(creators).build();
    }
}
