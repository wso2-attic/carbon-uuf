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

package org.wso2.carbon.uuf.sample.simpleauth.bundle.api.auth;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.exception.SessionManagementException;
import org.wso2.carbon.uuf.spi.auth.SessionManager;
import org.wso2.carbon.uuf.spi.auth.SessionManagerFactory;

import java.io.UncheckedIOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides PersistentSessionManagers for UUF apps.
 * <p>
 * Please make note to specify the session manager factory class name in the <tt>app.yaml</tt> configuration
 * file under the <tt>factoryClassName</tt> key in order for this session manager factory to be used in the application.
 * <p>
 * If the <tt>factoryClassName</tt> is not specified, {@link org.wso2.carbon.uuf.api.auth.InMemorySessionManagerFactory}
 * will be used as the session manager factory by default.
 * <p>
 * The session time-out duration (in seconds) can be specified in the <tt>app.yaml</tt> configuration file under
 * the 'timeout' key. This will make sure that the session file will be deleted in specified number of seconds. If this
 * value is not specified, the session timeout will be set to 20 minutes by default.
 * <p>
 * eg:
 * sessionManagement:
 * factoryClassName: "org.wso2.carbon.uuf.sample.simpleauth.bundle.api.auth.PersistentSessionManagerFactory"
 * timeout: 60 # This will keep the session file for 60 seconds
 *
 * @since 1.0.0
 */
@Component(name = "org.wso2.carbon.uuf.sample.simpleauth.bundle.api.auth.PersistentSessionManagerFactory",
        service = SessionManagerFactory.class,
        immediate = true
)
public class PersistentSessionManagerFactory implements SessionManagerFactory {

    private final ConcurrentMap<String, SessionManager> sessionManagers = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public SessionManager getSessionManager(String appName, Configuration configuration)
            throws SessionManagementException {
        try {
            return sessionManagers.computeIfAbsent(appName, name -> new PersistentSessionManager(name, configuration));
        } catch (UncheckedIOException e) {
            throw new SessionManagementException("Cannot create persistent session manager for app '" + appName + "'.",
                                                 e);
        }
    }
}
