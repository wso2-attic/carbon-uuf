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

import java.util.Optional;

/**
 * Bean class for holding the filter result.
 *
 * @since 1.0.0
 */
public class FilterResult {

    private Integer statusCode;
    private String message;
    private boolean isContinue;

    public FilterResult(boolean isContinue) {
        this.isContinue = isContinue;
    }

    public FilterResult(boolean isContinue, int statusCode) {
        this(isContinue);
        this.statusCode = statusCode;
    }

    public FilterResult(boolean isContinue, int statusCode, String message) {
        this(isContinue, statusCode);
        this.message = message;
    }

    /**
     * Get status code.
     *
     * @return optional status code
     */
    public Optional<Integer> getStatusCode() {
        return Optional.ofNullable(statusCode);
    }

    /**
     * Get message.
     *
     * @return optional message
     */
    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    /**
     * Get is continue.
     *
     * @return is continue
     */
    public boolean isContinue() {
        return isContinue;
    }

    /**
     * Set result message.
     *
     * @param message result message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
