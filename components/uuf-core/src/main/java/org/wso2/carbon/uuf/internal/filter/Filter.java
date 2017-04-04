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
import org.wso2.carbon.uuf.internal.RequestDispatcher;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;

/**
 * A filter is an object that performs filtering tasks based on the request and the UUF configuration.
 *
 * @since 1.0.0
 */
public interface Filter {

    /**
     * This method will be executed by the {@link RequestDispatcher} when the request is being dispatched.
     *
     * @param configuration final UUF configuration
     * @param request       http request instance
     * @param response      http response instance
     * @return Result after filtering
     */
    FilterResult doFilter(Configuration configuration, HttpRequest request, HttpResponse response);
}
