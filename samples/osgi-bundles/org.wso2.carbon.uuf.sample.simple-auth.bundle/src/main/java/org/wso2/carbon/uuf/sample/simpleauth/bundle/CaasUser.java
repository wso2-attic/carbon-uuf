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

import org.wso2.carbon.security.caas.api.CarbonPrincipal;
import org.wso2.carbon.security.caas.api.CarbonPermission;
import org.wso2.carbon.uuf.spi.auth.User;

public class CaasUser implements User {

    private final String username;
    private final CarbonPrincipal principal;

    public CaasUser(String username, CarbonPrincipal principal) {
        this.username = username;
        this.principal = principal;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean hasPermission(String resourceUri, String action) {
        CarbonPermission permission = new CarbonPermission(resourceUri, action);
        try {
            return principal.getUser().isUserAuthorized(permission);
         //TODO catch generic carbon-security exception once confirmed by the identity team
        } catch (Exception e) {
        }
        return false;
    }
}
