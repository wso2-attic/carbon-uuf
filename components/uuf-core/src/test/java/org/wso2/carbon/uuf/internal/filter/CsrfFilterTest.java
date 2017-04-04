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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.internal.auth.SessionRegistry;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link CsrfFilter}
 */
public class CsrfFilterTest {

    private Configuration configuration;
    private HttpRequest request;
    private HttpResponse response;
    private CsrfFilter csrfFilter;

    @BeforeMethod
    public void setup() {
        configuration = mock(Configuration.class);
        request = mock(HttpRequest.class);
        response = mock(HttpResponse.class);
        when(request.getUriWithoutContextPath()).thenReturn("/");
        csrfFilter = new CsrfFilter();
    }

    @Test
    public void doFilterGetRequest() {
        // Configuration settings
        when(configuration.getCsrfIgnoreUris()).thenReturn(Collections.emptySet()); // Not csrf

        // Http request settings
        when(request.isGetRequest()).thenReturn(true); // Get request
        when(request.getCookieValue(SessionRegistry.CSRF_TOKEN)).thenReturn(null); // No csrf token
        when(request.getFormParams()).thenReturn(Collections.emptyMap()); // No csrf tokens

        // Assert filter
        FilterResult result = csrfFilter.doFilter(configuration, request, response);
        Assert.assertEquals(result.isContinue(), true);
    }

    @Test
    public void doFilterPostRequestCsrfInIgnoreList() {
        // Configuration settings
        Set<UriPatten> patterns = new HashSet<>();
        patterns.add(new UriPatten("/"));
        when(configuration.getCsrfIgnoreUris()).thenReturn(patterns); // Not csrf

        // Http request settings
        when(request.isGetRequest()).thenReturn(false); // post request
        when(request.getCookieValue(SessionRegistry.CSRF_TOKEN)).thenReturn(null); // No csrf token
        when(request.getFormParams()).thenReturn(Collections.emptyMap()); // No csrf tokens

        // Assert filter
        FilterResult result = csrfFilter.doFilter(configuration, request, response);
        Assert.assertEquals(result.isContinue(), true);
    }

    @Test
    public void doFilterPostRequestCsrfNoToken() {
        // Configuration settings
        when(configuration.getCsrfIgnoreUris()).thenReturn(Collections.emptySet()); // Not csrf

        // Http request settings
        when(request.isGetRequest()).thenReturn(false); // post request
        when(request.getCookieValue(SessionRegistry.CSRF_TOKEN)).thenReturn(null); // No csrf token
        when(request.getFormParams()).thenReturn(Collections.emptyMap()); // No csrf tokens

        // Assert filter
        FilterResult result = csrfFilter.doFilter(configuration, request, response);
        Assert.assertEquals(result.isContinue(), false);
    }

    @Test
    public void doFilterPostRequestCsrfCookieTokenOnly() {
        // Configuration settings
        when(configuration.getCsrfIgnoreUris()).thenReturn(Collections.emptySet()); // Not csrf

        // Http request settings
        when(request.isGetRequest()).thenReturn(false); // post request
        when(request.getCookieValue(SessionRegistry.CSRF_TOKEN)).thenReturn("cookieToken"); // Cookie token
        when(request.getFormParams()).thenReturn(Collections.emptyMap()); // No csrf tokens

        // Assert filter
        FilterResult result = csrfFilter.doFilter(configuration, request, response);
        Assert.assertEquals(result.isContinue(), false);
    }

    @Test
    public void doFilterPostRequestCsrfFormParamTokenOnly() {
        // Configuration settings
        when(configuration.getCsrfIgnoreUris()).thenReturn(Collections.emptySet()); // Not csrf

        // Http request settings
        when(request.isGetRequest()).thenReturn(false); // post request
        when(request.getCookieValue(SessionRegistry.CSRF_TOKEN)).thenReturn(null); // No csrf token
        Map<String, Object> formParams = new HashMap<>();
        formParams.put("formParamToken", new Object());
        when(request.getFormParams()).thenReturn(formParams); // Form param token

        // Assert filter
        FilterResult result = csrfFilter.doFilter(configuration, request, response);
        Assert.assertEquals(result.isContinue(), false);
    }

    @Test
    public void doFilterPostRequestCsrfTokenMismatch() {
        // Configuration settings
        when(configuration.getCsrfIgnoreUris()).thenReturn(Collections.emptySet()); // Not csrf

        // Http request settings
        when(request.isGetRequest()).thenReturn(false); // post request
        when(request.getCookieValue(SessionRegistry.CSRF_TOKEN)).thenReturn("cookieToken"); // Cookie token
        Map<String, Object> formParams = new HashMap<>();
        formParams.put("formParamToken", new Object());
        when(request.getFormParams()).thenReturn(formParams); // Form param token

        // Assert filter
        FilterResult result = csrfFilter.doFilter(configuration, request, response);
        Assert.assertEquals(result.isContinue(), false);
    }

    @Test
    public void doFilterPostRequestCsrfTokenMatch() {
        final String token = "token";
        final String uufCsrfToken = "uuf-csrftoken";
        // Configuration settings
        when(configuration.getCsrfIgnoreUris()).thenReturn(Collections.emptySet()); // Not csrf

        // Http request settings
        when(request.isGetRequest()).thenReturn(false); // post request
        when(request.getCookieValue(SessionRegistry.CSRF_TOKEN)).thenReturn(token); // Cookie token
        Map<String, Object> formParams = new HashMap<>();
        formParams.put(uufCsrfToken, token);
        when(request.getFormParams()).thenReturn(formParams); // Form param token

        // Assert filter
        FilterResult result = csrfFilter.doFilter(configuration, request, response);
        Assert.assertEquals(result.isContinue(), true);
    }
}
