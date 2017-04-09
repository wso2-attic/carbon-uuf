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

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.kernel.context.PrivilegedCarbonContext;
import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

import org.wso2.carbon.security.caas.api.CarbonPrincipal;
import org.wso2.carbon.security.caas.api.ProxyCallbackHandler;

import org.wso2.carbon.security.caas.api.handler.UsernamePasswordCallbackHandler;
import org.wso2.carbon.uuf.api.auth.Session;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.Base64;

@Component(
        name = "org.wso2.carbon.uuf.sample.simpleauth.bundle.SimpleAuthHandler",
        immediate = true
)
public class SimpleAuthHandler {

    @Activate
    public void activate(BundleContext bundleContext) {
        // This config property will be read by Carbon-Security-Component only once at @Activate.
        // Hence need to set before activating Carbon-Security-Component.
        //TODO: Check once carbon-security component is adapted Startup-Order-Resolver
        System.setProperty("java.security.auth.login.config",
                           Utils.getCarbonConfigHome().resolve("security").resolve("carbon-jaas.config").toString()
        );
    }

    /**
     * Authenticate a user with this user name and password.
     *
     * @param userName user-name
     * @param password password
     * @throws LoginException
     */
    public static CaasUser authenticate(String userName, String password) throws LoginException {
        PrivilegedCarbonContext.destroyCurrentContext();

        CarbonMessage carbonMessage = new DefaultCarbonMessage();
        carbonMessage.setHeader("Authorization", "Basic " + Base64.getEncoder()
                .encodeToString((userName + ":" + password).getBytes())
        );

        ProxyCallbackHandler callbackHandler = new ProxyCallbackHandler(carbonMessage);
        LoginContext loginContext = new LoginContext("CarbonSecurityConfig", callbackHandler);
        loginContext.login();
        CarbonPrincipal principal = (CarbonPrincipal)PrivilegedCarbonContext.getCurrentContext().getUserPrincipal();

        CaasUser caas = new CaasUser(userName, principal);
        new Session(caas);
        return caas;
    }
}
