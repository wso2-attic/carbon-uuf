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
import org.wso2.carbon.uuf.api.auth.Permission;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.api.auth.User;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.internal.exception.HttpErrorException;
import org.wso2.carbon.uuf.internal.exception.PageRedirectException;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.auth.SessionManager;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for API.
 *
 * @since 1.0.0
 */
public class APITest {

    private static final String COOKIE_SESSION_ID = "UUFSESSIONID";
    private static final String COOKIE_CSRF_TOKEN = "CSRFTOKEN";

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
        // Creating user.
        User user = mock(User.class);
        when(user.getId()).thenReturn("admin");
        // Creating API.
        RequestLookup requestLookup = new RequestLookup("/test", request, response);
        SessionManager sessionManager = createSessionManager();
        mockCreateSession(user, sessionManager);
        API api = new API(sessionManager, null, requestLookup);

        // create session with null user
        Assert.assertThrows(IllegalArgumentException.class, () -> api.createSession(null));
        // create session
        Session createdSession = api.createSession(user);
        Assert.assertEquals(createdSession.getUser(), user);
    }

    @Test
    public void testGetSession() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getSessionTimeout()).thenReturn(600L);
        HttpRequest request = mock(HttpRequest.class);
        when(request.getContextPath()).thenReturn("/test");
        SessionManager sessionManager = createSessionManager();
        mockGetSession(sessionManager);
        API api = new API(sessionManager, null, new RequestLookup(null, request, null));
        Assert.assertEquals(api.getSession().isPresent(), true);
    }

    @Test
    public void testGetSessionWhenSessionNotAvailable() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getContextPath()).thenReturn("/test");
        SessionManager sessionManager = createSessionManager();
        when(sessionManager.getSession(request, null)).thenReturn(Optional.empty());
        API api = new API(sessionManager, null, new RequestLookup(null, request, null));
        Assert.assertEquals(api.getSession().isPresent(), false);
    }

    @Test
    public void testDestroySession() {
        // Creating session manager.
        Configuration configuration = mock(Configuration.class);
        when(configuration.getSessionTimeout()).thenReturn(600L);

        HttpRequest request = mock(HttpRequest.class);
        when(request.getContextPath()).thenReturn("/test");
        SessionManager sessionManager = createSessionManager();
        mockGetSession(sessionManager);
        mockDestroySession(sessionManager);

        // Creating API.
        RequestLookup requestLookup = new RequestLookup("/test", request, null);
        API api = new API(sessionManager, null, requestLookup);

        // Create session
        Assert.assertEquals(api.destroySession(), true);
    }

    @Test
    public void testIsAuthorizedWithAnyPermission() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getContextPath()).thenReturn("/test");
        SessionManager sessionManager = createSessionManager();
        mockGetSession(sessionManager);

        // Creating API.
        RequestLookup requestLookup = new RequestLookup("/test", request, null);
        API api = new API(sessionManager, null, requestLookup);

        Assert.assertTrue(api.hasPermission(Permission.ANY_PERMISSION));
    }

    @Test
    public void testIsAuthorizedWithNullAuthorizer() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getContextPath()).thenReturn("/test");
        SessionManager sessionManager = createSessionManager();
        mockGetSession(sessionManager);

        // Creating API.
        RequestLookup requestLookup = new RequestLookup("/test", request, null);
        API api = new API(sessionManager, null, requestLookup);

        // Other than for Permission.ANY_PERMISSION, the result should be false.
        Permission permission = mock(Permission.class);
        Assert.assertFalse(api.hasPermission(permission));
    }

    @Test
    public void testIsAuthorizedWithNoSession() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getContextPath()).thenReturn("/test");
        SessionManager sessionManager = createSessionManager();

        // Creating API.
        RequestLookup requestLookup = new RequestLookup("/test", request, null);
        API api = new API(sessionManager, null, requestLookup);
        Assert.assertFalse(api.hasPermission(null));
    }

    private SessionManager createSessionManager() {
        return mock(SessionManager.class);
    }

    private void mockCreateSession(User user, SessionManager sessionManager) {
        Session session = mock(Session.class);
        when(session.getUser()).thenReturn(user);
        when(sessionManager.createSession(any(), any(), any())).thenReturn(session);
    }

    private void mockGetSession(SessionManager sessionManager) {
        User user = mock(User.class);
        Session session = mock(Session.class);
        when(session.getUser()).thenReturn(user);
        when(sessionManager.getSession(any(), any())).thenReturn(Optional.of(session));
    }

    private void mockDestroySession(SessionManager sessionManager) {
        when(sessionManager.destroySession(any(), any())).thenReturn(true);
    }
}
