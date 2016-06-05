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

package org.wso2.carbon.uuf;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.HttpRequest;

import java.io.InputStream;
import java.util.Map;

public class RequestTest {

    @DataProvider(name = "uris")
    public static Object[][] getUris() {
        return new Object[][]{
                {"/a", true},
                {"/a/b", true},
                {"/a/b/c", true},
                {"/a/b/c/", true},
                {"/a/b.c", true},
                {"/a/b.c.", true},
                {"", false},
                {"a", false},
                {"a/b", false},
                {"//", false},
                {"/a/b///c", false},
                {"/.", false},
                {"/a..", false},
                {"/a/b/../d", false},
        };
    }

    @Test(dataProvider = "uris")
    public void testIsValid(String uri, boolean expectedResult) throws Exception {
        HttpRequest request = new HttpRequest() {
            @Override
            public String getUrl() {
                return null;
            }

            @Override
            public String getMethod() {
                return null;
            }

            @Override
            public String getProtocol() {
                return null;
            }

            @Override
            public Map<String, String> getHeaders() {
                return null;
            }

            @Override
            public String getHostName() {
                return null;
            }

            @Override
            public String getCookieValue(String cookieName) {
                return null;
            }

            @Override
            public String getUri() {
                return uri;
            }

            @Override
            public String getAppContext() {
                return null;
            }

            @Override
            public String getUriWithoutAppContext() {
                return null;
            }

            @Override
            public String getQueryString() {
                return null;
            }

            @Override
            public Map<String, Object> getQueryParams() {
                return null;
            }

            @Override
            public String getContentType() {
                return null;
            }

            @Override
            public String getContent() {
                return null;
            }

            @Override
            public byte[] getContentBytes() {
                return new byte[0];
            }

            @Override
            public InputStream getInputStream() {
                return null;
            }

            @Override
            public long getContentLength() {
                return 0;
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public String getRemoteAddr() {
                return null;
            }

            @Override
            public String getContextPath() {
                return null;
            }

            @Override
            public int getLocalPort() {
                return 0;
            }
        };
        Assert.assertEquals(request.isValid(), expectedResult);
    }
}
