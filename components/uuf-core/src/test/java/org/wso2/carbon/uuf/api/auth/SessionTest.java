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

package org.wso2.carbon.uuf.api.auth;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for session.
 *
 * @since 1.0.0
 */
public class SessionTest {

    @Test
    public void testSessionIdValidation() {
        Assert.assertEquals(Session.isValidSessionId(null), false);
        Assert.assertEquals(Session.isValidSessionId(""), false);
        Assert.assertEquals(Session.isValidSessionId("1234567890123456789012345678901"), false);
        Assert.assertEquals(Session.isValidSessionId("123456789012345678901234567890123"), false);
        Assert.assertEquals(Session.isValidSessionId("12345678901234567890123456789012"), true);
        Assert.assertEquals(Session.isValidSessionId("2B2F3466F1937F70B50A610453509EEB"), true);
    }
}
