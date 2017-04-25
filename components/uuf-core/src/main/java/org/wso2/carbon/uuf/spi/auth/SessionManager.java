/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.spi.auth;

import org.wso2.carbon.uuf.api.auth.SessionHandler;
import org.wso2.carbon.uuf.api.config.Configuration;

import java.io.Closeable;

/**
 * Manage session instances.
 *
 * @since 1.0.0
 */
public interface SessionManager extends SessionHandler, Closeable {

    /**
     * Initialize the session manager instance.
     *
     * @param configuration app configuration
     */
    void init(Configuration configuration);

    /**
     * Delete all the sessions.
     */
    void clear();

    /**
     * Get the total number of active sessions.
     *
     * @return number of active sessions
     */
    int getCount();

    /**
     *
     */
    void close();
}
