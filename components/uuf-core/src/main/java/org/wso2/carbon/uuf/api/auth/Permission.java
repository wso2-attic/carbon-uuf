/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.api.auth;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a permission of a resource.
 *
 * @since 1.0.0
 */
public class Permission implements Serializable {

    /**
     * Permission representing any permission for a resource.
     */
    public static final Permission ANY_PERMISSION = new Permission("*", "*");

    private final String resourceUri;
    private final String action;

    /**
     * Constructs a permission for a given resource.
     *
     * @param resourceUri URI of the resource
     * @param action      action on the resource
     */
    public Permission(String resourceUri, String action) {
        this.resourceUri = resourceUri;
        this.action = action;
    }

    /**
     * Returns the resource URI of this permission.
     *
     * @return resource URI of this permission
     */
    public String getResourceUri() {
        return resourceUri;
    }

    /**
     * Returns the action of this permission.
     *
     * @return action applicable of this permission
     */
    public String getAction() {
        return action;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Permission)) {
            return false;
        }
        Permission other = (Permission) obj;
        return Objects.equals(resourceUri, other.resourceUri) && Objects.equals(action, other.action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(resourceUri, action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{\"resourceUri\":" + resourceUri + ", \"action\": " + action + "}";
    }
}
