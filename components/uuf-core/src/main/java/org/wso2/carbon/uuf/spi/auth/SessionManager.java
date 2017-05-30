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

import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.api.auth.User;
import org.wso2.carbon.uuf.api.exception.SessionManagementException;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;

import java.util.Optional;

/**
 * Manages user sessions for a single UUF app.
 *
 * @since 1.0.0
 */
public interface SessionManager {

    /**
     * Creates a new session for the specified user.
     *
     * @param user     user of the session
     * @param request  HTTP request
     * @param response HTTP response
     * @return created session
     * @throws SessionManagementException if the creation of the session fails
     */
    Session createSession(User user, HttpRequest request, HttpResponse response) throws SessionManagementException;

    /**
     * Returns the current session of the specified request.
     *
     * @param request  HTTP request
     * @param response HTTP response
     * @return UUF session
     * @throws SessionManagementException if obtaining the session fails
     */
    Optional<Session> getSession(HttpRequest request, HttpResponse response) throws SessionManagementException;

    /**
     * Destroys the current session of the specified request.
     *
     * @param request  HTTP request
     * @param response HTTP response
     * @return {@code true} if the session is successfully destroyed, {@code false} otherwise
     * @throws SessionManagementException if destroying the session fails
     */
    boolean destroySession(HttpRequest request, HttpResponse response) throws SessionManagementException;

    /**
     * Returns number of active sessions managed by this session manager.
     *
     * @return number of active sessions
     */
    int getCount();
}
