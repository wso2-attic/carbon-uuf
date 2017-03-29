/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.internal.auth;

import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.auth.SessionManager;
import org.wso2.carbon.uuf.spi.auth.User;

import java.util.Optional;

import static org.wso2.carbon.uuf.spi.HttpRequest.COOKIE_CSRFTOKEN;
import static org.wso2.carbon.uuf.spi.HttpRequest.COOKIE_UUFSESSIONID;

public class SessionRegistry {

    public static final String SESSION_COOKIE_NAME = COOKIE_UUFSESSIONID;
    public static final String CSRF_TOKEN = COOKIE_CSRFTOKEN;
    private SessionManager sessionManager = null;
    private String appName;


    public SessionRegistry(String name, SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.appName = name;
    }

    public Session addSession(User user, HttpRequest request, HttpResponse response) {
        return sessionManager.createSession(appName, user, request, response);
    }

    public Optional<Session> getSession(HttpRequest request, HttpResponse response) {
        return sessionManager.getSession(appName, request, response);
    }

    public boolean removeSession(HttpRequest request, HttpResponse response) {
        return sessionManager.removeSession(appName, request, response);
    }

}
