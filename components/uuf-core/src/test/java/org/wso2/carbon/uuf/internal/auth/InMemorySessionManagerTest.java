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
import org.wso2.carbon.uuf.spi.auth.User;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for default session manager.
 *
 * @since 1.0.0
 */
public class InMemorySessionManagerTest {

    private static SessionManager createSessionManager() {
        Configuration configuration = mock(Configuration.class);
        SessionManager sessionManager = new InMemorySessionManager();
        sessionManager.init(configuration);
        return sessionManager;
    }

    @Test
    public void testSessionAddAndRemove() {
        SessionManager sessionManager = createSessionManager();
        User user = mock(User.class);
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        when(request.getContextPath()).thenReturn("/testSessionAddAndRemove");

        Session session = sessionManager.createSession(user, request, response);
        when(request.getCookieValue(Session.SESSION_COOKIE_NAME))
                .thenReturn(session.getSessionId());
        Assert.assertEquals(sessionManager.getSession(request, response).get(), session);

        boolean isDestroyed = sessionManager.destroySession(request, response);
        Assert.assertEquals(isDestroyed, true);
        Assert.assertEquals(sessionManager.getSession(request, response).isPresent(), false);
    }

    @Test
    public void testSessionEncapsulation() {
        SessionManager sessionManager = createSessionManager();
        User user = mock(User.class);

        // Session for context path A
        HttpRequest requestContextPathA = mock(HttpRequest.class);
        when(requestContextPathA.getContextPath()).thenReturn("/contextPathA");
        Session sessionContextPathA = sessionManager.createSession(user, requestContextPathA, null);
        when(requestContextPathA.getCookieValue(Session.SESSION_COOKIE_NAME))
                .thenReturn(sessionContextPathA.getSessionId());

        // Session for context path B
        HttpRequest requestContextPathB = mock(HttpRequest.class);
        when(requestContextPathB.getContextPath()).thenReturn("/contextPathB");
        Session sessionContextPathB = sessionManager.createSession(user, requestContextPathB, null);
        when(requestContextPathB.getCookieValue(Session.SESSION_COOKIE_NAME))
                .thenReturn(sessionContextPathB.getSessionId());

        Assert.assertNotEquals(sessionManager.getSession(requestContextPathA, null).get(),
                sessionManager.getSession(requestContextPathB, null).get());

        // Try to get a session for a different context path
        HttpRequest requestContextPathC = mock(HttpRequest.class);
        when(requestContextPathC.getContextPath()).thenReturn("/contextPathC");
        Assert.assertEquals(sessionManager.getSession(requestContextPathC, null).isPresent(), false);
    }

}
