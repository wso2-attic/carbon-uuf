/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.uuf.spi.auth;

import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;

public interface Authenticator {

    Result login(Configuration configuration, API api, HttpRequest request, HttpResponse response)
            throws UUFException;

    Result logout(Configuration configuration, API api, HttpRequest request, HttpResponse response)
            throws UUFException;

    public static class Result {
        private Status status;
        private String redirectURL;
        private String errorMessage;
        private User user;

        public Result(Status status, String redirectURL, String errorMessage, User user) {
            this.status = status;
            this.redirectURL = redirectURL;
            this.errorMessage = errorMessage;
            this.user = user;
        }

        public String getRedirectURL() {
            return redirectURL;
        }

        public void setRedirectURL(String redirectURL) {
            this.redirectURL = redirectURL;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    public enum Status {
        CONTINUE,
        REDIRECT,
        SUCESS,
        ERROR
    }
}
