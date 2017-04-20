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

package org.wso2.carbon.uuf.spi.auth;


import java.io.Serializable;

/**
 * Represents an user.
 *
 * @since 1.0.0
 */
public interface User extends Serializable {

    /**
     * Returns the username of this user.
     *
     * @return username of this user
     */
    String getUsername();

    /**
     * Checks whether this user has the specified permission.
     * @param resourceUri resource of the permission to be checked
     * @param action action of the permission to be checked
     * @return {@code true} if this user has the permission, {@code false} if not
     */
    boolean hasPermission(String resourceUri, String action);

    /**
     * {@inheritDoc}
     */
    int hashCode();

    /**
     * {@inheritDoc}
     */
    boolean equals(Object obj);

    /**
     * {@inheritDoc}
     */
    String toString();
}
