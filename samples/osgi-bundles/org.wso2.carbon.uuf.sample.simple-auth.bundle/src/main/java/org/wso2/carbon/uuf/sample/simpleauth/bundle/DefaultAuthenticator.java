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
package org.wso2.carbon.uuf.sample.simpleauth.bundle;

import org.wso2.carbon.kernel.context.PrivilegedCarbonContext;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.security.caas.api.CarbonPrincipal;
import org.wso2.carbon.security.caas.api.ProxyCallbackHandler;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.auth.Authenticator;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.Base64;

public class DefaultAuthenticator implements Authenticator {


    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC = "Basic ";
    private static final String CARBON_SECURITY_CONFIG = "CarbonSecurityConfig";
    private static final String POST = "POST";

    @Override
    public Result login(Configuration configuration, API api, HttpRequest request, HttpResponse response)
            throws UUFException {
        Result result = null;
        if (isNotPostRequest(request)) {
            result = new Result(Status.CONTINUE, null, null, null);
            return result;
        }
        api.destroySession();

        if (request.getFormParams().get(USERNAME) != null && request.getFormParams().get(PASSWORD) != null) {
            PrivilegedCarbonContext.destroyCurrentContext();
            CarbonMessage carbonMessage = new DefaultCarbonMessage();
            carbonMessage.setHeader(AUTHORIZATION, BASIC + Base64.getEncoder()
                    .encodeToString((request.getFormParams().get(USERNAME).toString() + ":" +
                            request.getFormParams().get(PASSWORD).toString()).getBytes())
            );

            ProxyCallbackHandler callbackHandler = new ProxyCallbackHandler(carbonMessage);

            try {
                LoginContext loginContext = new LoginContext(CARBON_SECURITY_CONFIG, callbackHandler);
                loginContext.login();
            } catch (LoginException e) {
                result = new Result(Status.ERROR, null, e.getMessage(), null);
                return result;
            }
            CarbonPrincipal principal =
                    (CarbonPrincipal) PrivilegedCarbonContext.getCurrentContext().getUserPrincipal();

            CaasUser caas = new CaasUser(request.getFormParams().get(USERNAME).toString(), principal);
            api.createSession(caas);
            result = new Result(Status.SUCESS, null, null, caas);
            return result;
        }
        return result;
    }

    @Override
    public Result logout(Configuration configuration, API api, HttpRequest request, HttpResponse response)
            throws UUFException {
        api.destroySession();
        Result result = new Result(Status.SUCESS,
                                   request.getContextPath() + configuration.other().get("loginRedirectUri").toString(),
                                   null, null);
        return result;
    }

    private boolean isNotPostRequest(HttpRequest request) {
        return request.getMethod() == null || !POST.equalsIgnoreCase(request.getMethod());
    }
}
