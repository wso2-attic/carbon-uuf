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

package org.wso2.carbon.uuf.sample.simpleauth.bundle;

import java.util.ArrayList;
import java.util.List;

public class UserManager {
    /**
     * Returns all users.
     *
     * @return list of users
     */
    public List<User> getAllUsers() {
        // TODO: 2016/07/25 Change this to call JAAS API
        List<User> users = new ArrayList<>();
        users.add(new User("john", "john@abc.com", "ABC Organization"));
        users.add(new User("ann", "ann@ghi.com", "GHI Organization"));
        users.add(new User("steve", "steve@def.com", "DEF Organization"));
        users.add(new User("anna", "anna@abc.com", "ABC Organization"));
        return users;
    }

    public class User {
        private final String name;
        private final String email;
        private final String organization;

        public User(String name, String email, String organization) {
            this.name = name;
            this.email = email;
            this.organization = organization;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getOrganization() {
            return organization;
        }
    }
}
