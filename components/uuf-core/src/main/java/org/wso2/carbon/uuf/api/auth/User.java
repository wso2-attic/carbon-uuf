/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.api.auth;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an user.
 *
 * @since 1.0.0
 */
public class User implements Serializable {

    private final String id;
    private final Map<String, Serializable> properties;

    /**
     * Constructs an user.
     *
     * @param id         unique id representing the user
     * @param properties properties for the user
     */
    public User(String id, Map<String, Serializable> properties) {
        this.id = id;
        this.properties = new HashMap<>(properties);
    }

    /**
     * Returns the unique id of this user.
     *
     * @return unique id of this user
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the value for the given property key.
     *
     * @param propertyKey the key whose associated value is to be returned
     * @return property value of the given key
     */
    public Serializable getProperty(String propertyKey) {
        return properties.get(propertyKey);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return Objects.hash(id, properties);
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        return (obj instanceof User) && Objects.equals(id, ((User) obj).id);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "{\"id\":" + id + "}";
    }
}
