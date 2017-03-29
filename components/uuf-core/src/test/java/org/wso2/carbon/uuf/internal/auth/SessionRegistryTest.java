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
import org.wso2.carbon.uuf.api.auth.DefaultSessionManager;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.auth.SessionManager;
import org.wso2.carbon.uuf.spi.auth.User;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for bindings.
 *
 * @since 1.0.0
 */
public class SessionRegistryTest {

    private static SessionRegistry createSessionRegistry() {
        SessionManager sessionManager = new DefaultSessionManager();
        return new SessionRegistry("test",sessionManager);
    }

    @Test
    public void testSessionAddAndRemove() {
        // Creating request.
        HttpRequest request = mock(HttpRequest.class);
        when(request.getContextPath()).thenReturn("/test");
        // Creating response.
        HttpResponse response = mock(HttpResponse.class);
        final Map<String, String> cookies = new HashMap<>();
        doAnswer(invocation -> {
            cookies.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(response).addCookie(any(), any());
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("admin");
        SessionRegistry sessionRegistry = createSessionRegistry();

        Session session = sessionRegistry.addSession(user, request, response);
        when(request.getCookieValue(eq(SessionRegistry.SESSION_COOKIE_NAME)))
                .thenReturn(session.getSessionId());
        Assert.assertEquals(sessionRegistry.getSession(request, response).get(), session);
        sessionRegistry.removeSession(request, response);
        Assert.assertEquals(sessionRegistry.getSession(request, response).isPresent(), false);
    }
}
