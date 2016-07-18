/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

function onRequest(env) {
    var session = getSession();
    if (session) {
        sendRedirect(env.contextPath + env.config['loginRedirectUri']);
    }
    if (env.request.method === "POST") {
        var username = env.request.formParams['username'];
        var password = env.request.formParams['password'];
        // calling dummy authenticate service
        var result = authenticate(username, password);
        if (result.success) {
            //configure login redirect uri
            //sendRedirect(env.contextPath + env.config['loginRedirectUri'] + '?username=' + username);
            sendRedirect(env.contextPath + env.config['loginRedirectUri']);
        } else {
            // {success: true, message: ""};
            return {login: result};
        }
    }
}

function authenticate(username, password) {
    if (username === "admin" && password === "admin") {
        createSession(username);
        return {success: true, message: "success"}
    }
    return {success: false, message: "Incorrect username and password combination."};
}