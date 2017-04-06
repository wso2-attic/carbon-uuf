/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.internal.filter;

import com.google.common.collect.ImmutableSet;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.internal.auth.SessionRegistry;
import org.wso2.carbon.uuf.spi.HttpRequest;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link CsrfFilter}
 */
public class CsrfFilterTest {

    private static final String CSRF_IGNORE_URI = "/csrfIgnoreUrl";

    @DataProvider
    public Object[][] requestData() {
        // GET HTTP request
        HttpRequest getRequest = mock(HttpRequest.class);
        when(getRequest.isGetRequest()).thenReturn(true);

        // POST HTTP request with CSRF ignore url
        HttpRequest postRequestWithCsrfIgnoreUrl = createPostHttpRequest(CSRF_IGNORE_URI);

        // POST HTTP request
        HttpRequest postRequest = createPostHttpRequest("/a");

        // POST HTTP request with CSRF cookie token
        HttpRequest postRequestWithCsrfCookieToken = createPostHttpRequest("/a");
        when(postRequestWithCsrfCookieToken.getCookieValue(HttpRequest.COOKIE_CSRFTOKEN))
                .thenReturn(mock(Session.class).getCsrfToken());

        // POST HTTP request with CSRF form param token
        HttpRequest postRequestWithCsrfFormParamToken = createPostHttpRequest("/a");
        when(postRequestWithCsrfFormParamToken.getFormParams())
                .thenReturn(Collections.singletonMap(HttpRequest.COOKIE_CSRFTOKEN, mock(Session.class).getCsrfToken()));

        // POST HTTP request with CSRF token mismatch
        HttpRequest postRequestTokenMismatch = createPostHttpRequest("/a");
        when(postRequestTokenMismatch.getCookieValue(HttpRequest.COOKIE_CSRFTOKEN))
                .thenReturn(mock(Session.class).getCsrfToken());
        when(postRequestTokenMismatch.getFormParams())
                .thenReturn(Collections.singletonMap(HttpRequest.COOKIE_CSRFTOKEN, mock(Session.class).getCsrfToken()));

        // POST HTTP request with CSRF token match
        String token = mock(Session.class).getCsrfToken();
        HttpRequest postRequestTokenMatch = createPostHttpRequest("/a");
        when(postRequestTokenMatch.getCookieValue(HttpRequest.COOKIE_CSRFTOKEN)).thenReturn(token);
        when(postRequestTokenMatch.getFormParams())
                .thenReturn(Collections.singletonMap(HttpRequest.COOKIE_CSRFTOKEN, token));

        return new Object[][]{
                {getRequest, true},
                {postRequestWithCsrfIgnoreUrl, true},
                {postRequest, false},
                {postRequestWithCsrfCookieToken, false},
                {postRequestWithCsrfFormParamToken, false},
                {postRequestTokenMismatch, false},
                {postRequestTokenMatch, true}
        };
    }

    @Test(dataProvider = "requestData")
    public void testFiltering(HttpRequest request, boolean assertion) {
        Configuration configuration = createConfiguration();
        CsrfFilter csrfFilter = new CsrfFilter();
        FilterResult result = csrfFilter.doFilter(request, configuration);
        Assert.assertEquals(result.isContinue(), assertion);
    }

    /**
     * Create a configuration instance.
     *
     * @return configuration instance
     */
    private static Configuration createConfiguration() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getCsrfIgnoreUris()).thenReturn(ImmutableSet.of(new UriPatten(CSRF_IGNORE_URI)));
        return configuration;
    }

    /**
     * Create a POST HTTP request.
     *
     * @param uriWithoutContextPath URI without the context path
     * @return POST HTTP request
     */
    private static HttpRequest createPostHttpRequest(String uriWithoutContextPath) {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getUriWithoutContextPath()).thenReturn(uriWithoutContextPath);
        when(request.isGetRequest()).thenReturn(false);
        return request;
    }
}
