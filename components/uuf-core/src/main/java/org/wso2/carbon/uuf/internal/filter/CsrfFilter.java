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

import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.internal.auth.SessionRegistry;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;

import java.util.Set;

import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_UNAUTHORIZED;

/**
 * This filter is responsible for filtering uri's with Cross-Site Request Forgery (CSRF) vulnerabilities.
 *
 * @since 1.0.0
 */
public class CsrfFilter implements Filter {

    private static final String UUF_CSRF_TOKEN = "uuf-csrftoken";

    @Override
    public FilterResult doFilter(Configuration configuration, HttpRequest request, HttpResponse response) {
        boolean isCsrfVulnerableUri = !request.isGetRequest() &&
                !anyMatch(configuration.getCsrfIgnoreUris(), request.getUriWithoutContextPath());
        // POST request where the URI isn't in the CSRF ignore list, hence validate the CSRF Token
        if (isCsrfVulnerableUri && (request.getCookieValue(SessionRegistry.CSRF_TOKEN) == null ||
                request.getFormParams().get(UUF_CSRF_TOKEN) == null ||
                !request.getFormParams().get(UUF_CSRF_TOKEN)
                        .equals(request.getCookieValue(SessionRegistry.CSRF_TOKEN)))) {
            return new FilterResult(false, STATUS_UNAUTHORIZED, "The requested uri pattern is " +
                    "depicts a possible Cross-Site Request Forgery (CSRF) attack.");
        }
        return new FilterResult(true);
    }

    /**
     * Check the current uri matches any of the given uri patterns.
     *
     * @param uris uri set
     * @param uri  uri string for matching against uris
     * @return true if there is any match against the given uri set
     */
    private boolean anyMatch(Set<UriPatten> uris, String uri) {
        for (UriPatten uriPatten : uris) {
            if (uriPatten.matches(uri)) {
                return true;
            }
        }
        return false;
    }
}
