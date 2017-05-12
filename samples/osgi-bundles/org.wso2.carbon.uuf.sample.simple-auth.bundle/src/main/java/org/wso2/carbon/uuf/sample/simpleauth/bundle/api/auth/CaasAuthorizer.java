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

package org.wso2.carbon.uuf.sample.simpleauth.bundle.api.auth;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.kernel.context.CarbonContext;
import org.wso2.carbon.security.caas.api.CarbonPermission;
import org.wso2.carbon.security.caas.api.CarbonPrincipal;
import org.wso2.carbon.uuf.api.auth.Permission;
import org.wso2.carbon.uuf.api.auth.User;
import org.wso2.carbon.uuf.exception.UnauthorizedException;
import org.wso2.carbon.uuf.spi.auth.Authorizer;

/**
 * Manages authorization for resources.
 * <p>
 * This authorizer will take advantage of the <tt>org.wso2.carbon.security.caas.api</tt> authorization implementation.
 * <p>
 * Please make note to specify the authorizer class name in the <tt>app.yaml</tt> configuration file under the
 * <tt>authorizer</tt> key in order for this authorizer to be used in the application.
 * <p>
 * eg:
 * authorizer: "org.wso2.carbon.uuf.sample.simpleauth.bundle.api.auth.CaasAuthorizer"
 *
 * @since 1.0.0
 */
@Component(name = "org.wso2.carbon.uuf.sample.simpleauth.bundle.api.auth.CaasAuthorizer",
        service = Authorizer.class,
        immediate = true
)
public class CaasAuthorizer implements Authorizer {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPermission(User user, Permission permission) {
        if (user == null) {
            return false;
        }
        if (permission.getResourceUri() == null || permission.getResourceUri().trim().isEmpty()) {
            throw new UnauthorizedException("Permission resource URI cannot be null or empty.");
        }
        CarbonPrincipal carbonPrincipal = (CarbonPrincipal) CarbonContext.getCurrentContext().getUserPrincipal();
        CarbonPermission carbonPermission = new CarbonPermission(permission.getResourceUri(), permission.getAction());
        // TODO: 09/05/2017 - CarbonPrincipal.hasPermission always returns false according to CAAS implementation
        return carbonPrincipal.isAuthorized(carbonPermission);
    }
}
