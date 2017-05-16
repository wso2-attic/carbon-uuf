/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.uuf.renderablecreator.hbs.impl.js;

import org.wso2.carbon.uuf.renderablecreator.hbs.internal.serialize.JsonSerializer;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LoggerObject {

    public static final String NAME = "Log";

    private final org.slf4j.Logger logger;

    LoggerObject(String name) {
        this.logger = org.slf4j.LoggerFactory.getLogger(name);
    }

    private static String getLogMessage(Object obj) {
        return JsonSerializer.toPrettyJson(obj);
    }

    public void info(Object obj) {
        logger.info(getLogMessage(obj));
    }

    public void debug(Object obj) {
        logger.debug(getLogMessage(obj));
    }

    public void trace(Object obj) {
        logger.trace(getLogMessage(obj));
    }

    public void warn(Object obj) {
        logger.warn(getLogMessage(obj));
    }

    public void error(Object obj) {
        logger.error(getLogMessage(obj));
    }

    public void error(String message, Object obj) {
        if (obj instanceof Throwable) {
            logger.error(message, (Throwable) obj);
        } else {
            StringWriter stringWriter = new StringWriter();
            stringWriter.write(message);
            stringWriter.write(" ");
            stringWriter.write(getLogMessage(obj));

            PrintWriter printWriter = new PrintWriter(stringWriter);
            StackTracePrinter stackTracePrinter = new StackTracePrinter();
            // Ignore the 0th element and print the stack trace from the 1st index to the printWriter.
            stackTracePrinter.printStackTrace(printWriter, 1);
            logger.error(stringWriter.toString());
        }
    }

    @Override
    public String toString() {
        return "{info: function(obj), debug: function(obj), trace: function(obj), warn: function(obj), error: " +
                "function(obj), error: function(message, obj)}";
    }

    /**
     * Uses to get a modified stack trace.
     *
     * @since 1.0.0
     */
    private static class StackTracePrinter extends Exception {

        /**
         * Properly formats and prints the stack trace to the given {@link PrintWriter} starting from the  given index.
         *
         * @param printWriter which uses to write the stack trace
         * @param startIndex  start index of the stack trace to be printed
         */
        void printStackTrace(PrintWriter printWriter, int startIndex) {
            StackTraceElement[] stackTrace = getStackTrace();
            StackTraceElement[] modifiedStackTrace = new StackTraceElement[stackTrace.length - 1];
            System.arraycopy(stackTrace, startIndex, modifiedStackTrace, 0, stackTrace.length - 1);
            this.setStackTrace(modifiedStackTrace);
            this.printStackTrace(printWriter);
        }

        @Override
        public String toString() {
            return "";
        }
    }
}
