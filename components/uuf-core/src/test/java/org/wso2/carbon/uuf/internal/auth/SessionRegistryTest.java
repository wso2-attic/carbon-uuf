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

package org.wso2.carbon.uuf.internal.auth;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.auth.Session;

/**
 * Test cases for bindings.
 *
 * @since 1.0.0
 */
public class SessionRegistryTest {

    private static SessionRegistry createSessionRegistry() {
        return new SessionRegistry("test", 2);
    }

    private static Session createSession() {
        return new Session(null);
    }

    @Test
    public void testSessionAddAndRemove() {
        SessionRegistry sessionRegistry = createSessionRegistry();
        Session session = createSession();

        sessionRegistry.addSession(session);
        Assert.assertEquals(sessionRegistry.getSession(session.getSessionId()).get(), session);
        sessionRegistry.removeSession(session.getSessionId());
        Assert.assertEquals(sessionRegistry.getSession(session.getSessionId()).isPresent(), false);
    }

    @Test
    public void testSessionTimeout() throws InterruptedException {
        SessionRegistry sessionRegistry = createSessionRegistry(); // session timeout is 2 seconds
        Session session = createSession();

        sessionRegistry.addSession(session);
        Assert.assertEquals(sessionRegistry.getSession(session.getSessionId()).get(), session);
        Thread.sleep(4 * 1000); // wait 4 seconds.
        Assert.assertEquals(sessionRegistry.getSession(session.getSessionId()).isPresent(), false);
    }
}
