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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.spi.HttpRequest;

import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_FORBIDDEN;

/**
 * This filter is responsible for filtering URIs with Cross-Site Request Forgery (CSRF) attacks.
 *
 * @since 1.0.0
 */
public class CsrfFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrfFilter.class);

    @Override
    public FilterResult doFilter(HttpRequest request, Configuration configuration) {
        if (request.isGetRequest()) {
            return FilterResult.success();
        }
        for (UriPatten uriPatten : configuration.getCsrfIgnoreUris()) {
            if (uriPatten.matches(request.getUriWithoutContextPath())) {
                return FilterResult.success();
            }
        }

        // POST request where the URI isn't in the CSRF ignore list, hence validate the CSRF token.
        Object formPostCsrfToken = request.getFormParams().get(HttpRequest.COOKIE_CSRFTOKEN);
        if (formPostCsrfToken == null || !(formPostCsrfToken instanceof String) ||
                ((String) formPostCsrfToken).isEmpty()) {
            String error = createCsrfErrorMessage("Couldn't find the CSRF token in form params.");
            LOGGER.warn("{} - {form param token: {}, request: {}}", error, formPostCsrfToken, request);
            return FilterResult.error(STATUS_FORBIDDEN, error);
        }

        String cookieCsrfToken = request.getCookieValue(HttpRequest.COOKIE_CSRFTOKEN);
        if (cookieCsrfToken == null || cookieCsrfToken.isEmpty()) {
            String error = createCsrfErrorMessage("Couldn't find the CSRF token in cookie.");
            LOGGER.warn("{} - {cookie token: {}, form param token: {}, request: {}}", error, cookieCsrfToken,
                    formPostCsrfToken, request);
            return FilterResult.error(STATUS_FORBIDDEN, error);
        }

        if ((!formPostCsrfToken.equals(cookieCsrfToken))) {
            String error = createCsrfErrorMessage("CSRF token values in form params and cookie do not match.");
            LOGGER.warn("{} - {cookie token: {}, form param token: {}, request: {}}", error, cookieCsrfToken,
                    formPostCsrfToken, request);
            return FilterResult.error(STATUS_FORBIDDEN, error);
        }
        return FilterResult.success();
    }

    /**
     * Create a Csrf error message according to the given reason.
     *
     * @param reason reason for failing CSRF filter
     * @return Csrf error message
     */
    private String createCsrfErrorMessage(String reason) {
        return reason + " Hence request depicts a possible Cross-Site Request Forgery (CSRF) attack.";
    }
}
