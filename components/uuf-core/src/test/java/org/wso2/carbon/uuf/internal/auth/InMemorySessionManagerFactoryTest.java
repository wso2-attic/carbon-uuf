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

package org.wso2.carbon.uuf.internal.auth;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.auth.InMemorySessionManagerFactory;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.auth.SessionManagerFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for the in-memory session manager factory.
 *
 * @since 1.0.0
 */
public class InMemorySessionManagerFactoryTest {

    private static final String SESSION_COOKIE_NAME = "UUFSESSIONID";

    @Test
    public void testGetSessionManager() {
        SessionManagerFactory sessionManagerFactory = new InMemorySessionManagerFactory();
        Configuration configuration = mock(Configuration.class);
        when(configuration.getSessionTimeout()).thenReturn(600L);

        // Session Manager for context path A
        HttpRequest requestContextPathA = mock(HttpRequest.class);
        when(requestContextPathA.getContextPath()).thenReturn("/contextPathA");

        // Check if the same session manager is returned in any occasion for the same app name
        Assert.assertEquals(sessionManagerFactory
                .getSessionManager(requestContextPathA.getContextPath(), configuration), sessionManagerFactory
                .getSessionManager(requestContextPathA.getContextPath(), configuration));

        // Session for context path B
        HttpRequest requestContextPathB = mock(HttpRequest.class);
        when(requestContextPathB.getContextPath()).thenReturn("/contextPathB");

        // Check if different session managers are given for different app names
        Assert.assertNotEquals(sessionManagerFactory
                .getSessionManager(requestContextPathA.getContextPath(), configuration), sessionManagerFactory
                .getSessionManager(requestContextPathB.getContextPath(), configuration));
    }
}
