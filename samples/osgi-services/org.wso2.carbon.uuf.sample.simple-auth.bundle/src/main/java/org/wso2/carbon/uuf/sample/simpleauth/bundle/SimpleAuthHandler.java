/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.uuf.sample.simpleauth.bundle;

import org.wso2.carbon.kernel.context.PrivilegedCarbonContext;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.security.caas.api.ProxyCallbackHandler;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.Base64;

public class SimpleAuthHandler {

    /**
     * Authenticate a user with this user name and password.
     *
     * @param userName user-name
     * @param password password
     * @throws LoginException
     */
    public static void authenticate(String userName, String password) throws LoginException {
        // TODO: 2016/07/25 Change this to call JAAS API
        if (!(userName.equals("admin") && password.equals("admin"))) {
            throw new LoginException("Incorrect username and password combination.");
        }
    }

    /**
     * Authenticate a user with this user name and password.
     *
     * @param userName user-name
     * @param password password
     * @throws LoginException
     */
    public static void authenticateByCaas(String userName, String password) throws LoginException {
        PrivilegedCarbonContext.destroyCurrentContext();
        CarbonMessage carbonMessage = new DefaultCarbonMessage();
        carbonMessage.setHeader("Authorization", "Basic " + Base64.getEncoder()
                .encodeToString((userName + ":" + password).getBytes())
        );

        ProxyCallbackHandler callbackHandler = new ProxyCallbackHandler(carbonMessage);
        LoginContext loginContext = new LoginContext("CarbonSecurityConfig", callbackHandler);
        loginContext.login();
    }
}
