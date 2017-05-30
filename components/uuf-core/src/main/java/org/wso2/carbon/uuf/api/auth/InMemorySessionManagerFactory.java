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

import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.api.exception.SessionManagementException;
import org.wso2.carbon.uuf.api.exception.UUFRuntimeException;
import org.wso2.carbon.uuf.spi.auth.SessionManager;
import org.wso2.carbon.uuf.spi.auth.SessionManagerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides InMemorySessionManagers for UUF apps.
 *
 * @since 1.0.0
 */
public class InMemorySessionManagerFactory implements SessionManagerFactory {

    private final ConcurrentMap<String, SessionManager> sessionManagers = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public SessionManager getSessionManager(String appName, Configuration configuration)
            throws SessionManagementException {
        try {
            return sessionManagers.computeIfAbsent(appName, name -> new InMemorySessionManager(name, configuration));
        } catch (UUFRuntimeException e) {
            throw new SessionManagementException("Cannot create a session manager for app '" + appName + "'.", e);
        }
    }
}
