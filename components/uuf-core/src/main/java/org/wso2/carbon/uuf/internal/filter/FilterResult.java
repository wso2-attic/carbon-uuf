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

import static org.wso2.carbon.uuf.spi.HttpResponse.STATUS_OK;

/**
 * Bean class for holding the result of a HTTP request filtering.
 *
 * @since 1.0.0
 */
public class FilterResult {

    private static final String SUCCESS_MESSAGE = "Successful";

    private final int httpStatusCode;
    private final String message;
    private final boolean isContinue;

    /**
     * Constructs a new FilterResult instance.
     *
     * @param isContinue     whether the filtering process should be continued
     * @param httpStatusCode HTTP status code to be returned after executing the filter
     * @param message        filter result message
     */
    public FilterResult(boolean isContinue, int httpStatusCode, String message) {
        this.isContinue = isContinue;
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }

    /**
     * Creates a new success FilterResult.
     *
     * @return success FilterResult
     */
    public static FilterResult success() {
        return new FilterResult(true, STATUS_OK, SUCCESS_MESSAGE);
    }

    /**
     * Creates a new error FilterResult.
     *
     * @return error FilterResult
     */
    public static FilterResult error(int httpStatusCode, String message) {
        return new FilterResult(false, httpStatusCode, message);
    }

    /**
     * Returns the HTTP status code of this filter result.
     *
     * @return http status code of the filter result
     */
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * Returns the filter result message.
     *
     * @return filter result message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns whether the filtering process should continue or not.
     *
     * @return whether the filtering process should continue or not
     */
    public boolean isContinue() {
        return isContinue;
    }
}
