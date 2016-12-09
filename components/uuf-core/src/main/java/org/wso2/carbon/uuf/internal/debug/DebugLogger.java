/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.internal.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.spi.HttpRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.wso2.carbon.uuf.internal.debug.LogEvent.EventType;

/**
 * Debug logger that can be used to log various events.
 *
 * @since 1.0.0
 */
public class DebugLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugLogger.class);
    private static final boolean IS_DEBUGGING_ENABLED = (Debugger.isDebuggingEnabled() || LOGGER.isDebugEnabled());

    private static final AtomicLong COUNTER = new AtomicLong(0);
    private static final ThreadLocal<Long> REQUEST_ID = new ThreadLocal<>();
    private static final ThreadLocal<SequenceId> SEQUENCE_ID = new ThreadLocal<>();
    private static final ThreadLocal<List<LogEvent>> LOG_EVENTS = new ThreadLocal<>();

    private static volatile List<LogEvent> lastRequestLogEvents;

    /**
     * Logs the start of dispatching the specified HTTP request.
     *
     * @param request HTTP request that will be dispatch
     */
    public static void startRequest(HttpRequest request) {
        if (IS_DEBUGGING_ENABLED) {
            // Setup thread local variables.
            REQUEST_ID.set(COUNTER.get());
            SEQUENCE_ID.set(new SequenceId());
            if (Debugger.isDebuggingEnabled()) {
                LOG_EVENTS.set(new ArrayList<>(32));
            }
            // Log event.
            logEvent(EventType.START_REQUEST, request);
        }
    }

    /**
     * Logs the end of dispatching the specified HTTP request.
     *
     * @param request dispatched HTTP request
     */
    public static void endRequest(HttpRequest request) {
        if (IS_DEBUGGING_ENABLED) {
            logEvent(EventType.END_PAGE, request);
            lastRequestLogEvents = LOG_EVENTS.get();
            // Cleanup thread local variables.
            REQUEST_ID.remove();
            SEQUENCE_ID.remove();
            LOG_EVENTS.remove();
        }
    }

    /**
     * Logs the start of rendering the specified page.
     *
     * @param page page that will be rendered
     */
    public static void startPage(Page page) {
        if (IS_DEBUGGING_ENABLED) {
            logEvent(EventType.START_PAGE, page);
        }
    }

    /**
     * Logs the end of rendering the specified page.
     *
     * @param page rendered page
     */
    public static void endPage(Page page) {
        if (IS_DEBUGGING_ENABLED) {
            logEvent(EventType.END_PAGE, page);
        }
    }

    /**
     * Logs the start of rendering the specified fragment.
     *
     * @param fragment fragment that will be rendered
     */
    public static void startFragment(Fragment fragment) {
        if (IS_DEBUGGING_ENABLED) {
            logEvent(EventType.START_FRAGMENT, fragment);
        }
    }

    /**
     * Logs the end of rendering the specified fragment.
     *
     * @param fragment rendered fragment
     */
    public static void endFragment(Fragment fragment) {
        if (IS_DEBUGGING_ENABLED) {
            logEvent(EventType.END_FRAGMENT, fragment);
        }
    }

    /**
     * Returns the log events of the last HTTP request.
     *
     * @return log events of the last HTTP request, or {@code null} if there are no logged events.
     */
    static List<LogEvent> getLastRequestLogEvents() {
        return lastRequestLogEvents;
    }

    private static void logEvent(EventType eventType, Object eventSource) {
        if (Debugger.isDebuggingEnabled()) {
            LOG_EVENTS.get().add(new LogEvent(REQUEST_ID.get(), System.nanoTime(), eventType, eventSource));
        } else if (LOGGER.isDebugEnabled()) {
            // UUF_LOG:<request_id>:<sequence_id>:<time>:<event_type>:<event_source>:
            LOGGER.debug("UUF_LOG:{}:{}:{}:{}:{}",
                         REQUEST_ID.get(), SEQUENCE_ID.get().increment(), System.nanoTime(), eventType, eventSource);
        }
    }

    /**
     * Sequence ID.
     *
     * @since 1.0.0
     */
    private static class SequenceId {

        private int value = -1;

        public SequenceId increment() {
            value++;
            return this;
        }

        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }
}
