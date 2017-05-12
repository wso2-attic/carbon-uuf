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
import java.util.Collections;
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
    private Map<String, Serializable> properties = new HashMap<>();

    /**
     * Constructs an user.
     *
     * @param id         unique id representing the user
     * @param properties properties for the user
     */
    public User(String id, Map<String, Serializable> properties) {
        this.id = id;
        this.properties = Collections.unmodifiableMap(properties);
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
     * Returns the properties related to this user.
     *
     * @return properties related to this user
     */
    public Map<String, Serializable> getProperties() {
        return properties;
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
