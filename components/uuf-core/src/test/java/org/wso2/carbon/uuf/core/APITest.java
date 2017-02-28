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

package org.wso2.carbon.uuf.core;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.exception.HttpErrorException;
import org.wso2.carbon.uuf.exception.PageRedirectException;
import org.wso2.carbon.uuf.internal.auth.SessionRegistry;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.auth.User;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for API.
 *
 * @since 1.0.0
 */
public class APITest {

    @Test
    public void testSendError() {
        Assert.assertThrows(IllegalArgumentException.class, () -> API.sendError(99, "Some error!"));
        Assert.assertThrows(IllegalArgumentException.class, () -> API.sendError(600, "Some error!"));
        Assert.assertThrows(IllegalArgumentException.class, () -> API.sendError(500, null));
        Assert.assertThrows(IllegalArgumentException.class, () -> API.sendError(500, ""));

        HttpErrorException exception = Assert.expectThrows(HttpErrorException.class,
                                                           () -> API.sendError(500, "Some internal server error!"));
        Assert.assertEquals(exception.getHttpStatusCode(), 500);
        Assert.assertEquals(exception.getMessage(), "Some internal server error!");
    }

    @Test
    public void testSendRedirect() {
        Assert.assertThrows(IllegalArgumentException.class, () -> API.sendRedirect(null));
        Assert.assertThrows(IllegalArgumentException.class, () -> API.sendRedirect(""));

        PageRedirectException pre = Assert.expectThrows(PageRedirectException.class,
                                                        () -> API.sendRedirect("/some/uri"));
        Assert.assertEquals(pre.getHttpStatusCode(), HttpResponse.STATUS_FOUND);
        Assert.assertEquals(pre.getRedirectUrl(), "/some/uri");
    }

    @Test
    public void testCreateSession() {
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
        // Creating API.
        RequestLookup requestLookup = new RequestLookup("/test", request, response);
        API api = new API(new SessionRegistry("test"), requestLookup);
        // Creating user.
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("admin");

        // create session with null user
        Assert.assertThrows(IllegalArgumentException.class, () -> api.createSession(null));
        // create session
        Session createdSession = api.createSession(user);
        Assert.assertEquals(createdSession.getUser(), user);
        Assert.assertEquals(cookies.get(SessionRegistry.SESSION_COOKIE_NAME),
                createdSession.getSessionId() + "; Path=" + requestLookup.getContextPath() + "; Secure; HTTPOnly");
        Assert.assertEquals(cookies.get(SessionRegistry.CSRF_TOKEN),
                createdSession.getCsrfToken() + "; Path=" + requestLookup.getContextPath() + "; Secure");
    }

    @Test
    public void testGetSession() {
        SessionRegistry sessionRegistry = new SessionRegistry("test");
        Session currentSession = new Session(null);
        sessionRegistry.addSession(currentSession);

        HttpRequest request = mock(HttpRequest.class);
        when(request.getContextPath()).thenReturn("/test");
        when(request.getCookieValue(eq(SessionRegistry.SESSION_COOKIE_NAME)))
                .thenReturn(currentSession.getSessionId());
        API api = new API(sessionRegistry, new RequestLookup(null, request, null));

        Assert.assertEquals(api.getSession().isPresent(), true);
    }

    @Test
    public void testGetSessionWhenSessionNotAvailable() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getContextPath()).thenReturn("/test");
        when(request.getCookieValue(eq(SessionRegistry.SESSION_COOKIE_NAME)))
                .thenReturn("2B2F3466F1937F70B50A610453509EEB");
        API api = new API(new SessionRegistry("test"), new RequestLookup(null, request, null));

        Assert.assertEquals(api.getSession().isPresent(), false);
    }

    @Test
    public void testDestroySession() {
        // Creating session registry.
        SessionRegistry sessionRegistry = new SessionRegistry("test");
        Session currentSession = new Session(null);
        sessionRegistry.addSession(currentSession);
        // Creating request.
        HttpRequest request = mock(HttpRequest.class);
        when(request.getContextPath()).thenReturn("/test");
        when(request.getCookieValue(eq(SessionRegistry.SESSION_COOKIE_NAME)))
                .thenReturn(currentSession.getSessionId());
        // Creating response.
        HttpResponse response = mock(HttpResponse.class);
        final Map<String, String> headers = new HashMap<>();
        doAnswer(invocation -> {
            headers.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(response).addCookie(any(), any());
        // Creating API.
        RequestLookup requestLookup = new RequestLookup("/test", request, response);
        API api = new API(new SessionRegistry("test"), requestLookup);

        Assert.assertEquals(api.destroySession(), true);
        Assert.assertEquals(headers.get(SessionRegistry.SESSION_COOKIE_NAME),
                "Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:01 GMT; Path=" + requestLookup.getContextPath() + "; Secure; HTTPOnly");
        Assert.assertEquals(headers.get(SessionRegistry.CSRF_TOKEN),
                "Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:01 GMT; Path=" + requestLookup.getContextPath() + "; Secure; HTTPOnly");
        Assert.assertEquals(api.getSession().isPresent(), false);
        Assert.assertEquals(sessionRegistry.getSession(currentSession.getSessionId()).isPresent(), false);
    }
}
