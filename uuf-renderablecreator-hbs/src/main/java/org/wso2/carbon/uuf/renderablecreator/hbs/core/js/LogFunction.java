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

package org.wso2.carbon.uuf.renderablecreator.hbs.core.js;

import java.util.Arrays;

@FunctionalInterface
public interface LogFunction {

    String NAME = "log";

    void call(Object... args);

    default LogLevel getLogLevel(Object arg0) {
        if (!(arg0 instanceof String)) {
            throw new IllegalArgumentException(
                    "Log level must be a string " + Arrays.toString(LogLevel.values()) + ". Instead found '" +
                            arg0.getClass().getName() + "'.");
        }
        try {
            return LogLevel.valueOf((String) arg0);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Log level must be " + Arrays.toString(LogLevel.values()) + ". Instead found '" + arg0 + "'.");
        }
    }

    enum LogLevel {
        INFO, DEBUG, TRACE, WARN, ERROR
    }
}
