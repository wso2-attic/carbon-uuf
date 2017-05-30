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

package org.wso2.carbon.uuf.sample.featuresapp.bundle.api.auth;

import com.google.common.collect.ImmutableMap;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.uuf.api.auth.Permission;
import org.wso2.carbon.uuf.api.auth.User;
import org.wso2.carbon.uuf.spi.auth.Authorizer;

import java.util.Map;

/**
 * Evaluates permissions for users.
 * <p>
 * Please make note to specify the authorizer class name in the <tt>app.yaml</tt> configuration file under the
 * <tt>authorizer</tt> key in order for this authorizer to be used in the app.
 * <p>
 * eg:
 * authorizer: "org.wso2.carbon.uuf.sample.featuresapp.bundle.api.auth.DemoAuthorizer"
 *
 * @since 1.0.0
 */
@Component(name = "org.wso2.carbon.uuf.sample.featuresapp.bundle.api.auth.DemoAuthorizer",
           service = Authorizer.class,
           immediate = true
)
// TODO: Write a proper CAAS Authorizer
public class DemoAuthorizer implements Authorizer {

    private static final Map<String, Permission> permissions =
            ImmutableMap.of("admin", new Permission("helpers/uuf-helpers/secured", "view"));

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPermission(User user, Permission permission) {
        if (user == null) {
            return false;
        }
        if (permission.getResourceUri() == null || permission.getResourceUri().trim().isEmpty()) {
            throw new IllegalArgumentException("Permission resource URI cannot be null or empty.");
        }
        for (Map.Entry<String, Permission> entry : permissions.entrySet()) {
            if (entry.getKey().equals(user.getId()) && entry.getValue().equals(permission)) {
                return true;
            }
        }
        return false;
    }
}
