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
import org.wso2.carbon.uuf.api.auth.InMemorySessionManager;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.auth.SessionManager;
import org.wso2.carbon.uuf.api.auth.User;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for the in-memory session manager.
 *
 * @since 1.0.0
 */
public class InMemorySessionManagerTest {

    private static final String SESSION_COOKIE_NAME = "UUFSESSIONID";

    private static SessionManager createSessionManager(String appName) {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getSessionTimeout()).thenReturn(600L);
        return new InMemorySessionManager(appName, configuration);
    }

    @Test
    public void testSessionAddAndRemove() {
        User user = mock(User.class);
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        when(request.getContextPath()).thenReturn("/testSessionAddAndRemove");
        SessionManager sessionManager = createSessionManager(request.getContextPath());

        Session session = sessionManager.createSession(user, request, response);
        when(request.getCookieValue(SESSION_COOKIE_NAME))
                .thenReturn(session.getSessionId());
        Assert.assertEquals(sessionManager.getSession(request, response).get(), session);

        boolean isDestroyed = sessionManager.destroySession(request, response);
        Assert.assertEquals(isDestroyed, true);
        Assert.assertEquals(sessionManager.getSession(request, response).isPresent(), false);
    }
}
