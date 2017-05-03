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

package org.wso2.carbon.uuf.spi.auth;

import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.exception.SessionManagementException;

/**
 * Provides session managers for UUF apps.
 * <p>
 * Implementations of this interface should be thread safe.
 *
 * @since 1.0.0
 */
public interface SessionManagerFactory {

    /**
     * Returns the session manager for the specified UUF app.
     *
     * @param appName       name of the UUF app
     * @param configuration app configuration
     * @return session manager for the specified app
     * @throws SessionManagementException if an error occurs when creating or returning an already created session manager
     */
    SessionManager getSessionManager(String appName, Configuration configuration) throws SessionManagementException;
}
