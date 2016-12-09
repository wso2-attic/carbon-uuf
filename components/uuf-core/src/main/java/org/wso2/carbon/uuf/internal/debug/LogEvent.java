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

/**
 * This class represents a log event.
 *
 * @since 1.0.0
 */
public class LogEvent {

    private final long id;
    private final long timestamp;
    private final EventType type;
    private final Object source;

    /**
     * Creates a new log event.
     *
     * @param id        ID of the event
     * @param timestamp time when the event happened
     * @param type      type of the event
     * @param source    source of the event
     */
    public LogEvent(Long id, long timestamp, EventType type, Object source) {
        this(id.longValue(), timestamp, type, source);
    }

    /**
     * Creates a new log event.
     *
     * @param id        ID of the event
     * @param timestamp time when the event happened
     * @param type      type of the event
     * @param source    source of the event
     */
    public LogEvent(long id, long timestamp, EventType type, Object source) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.source = source;
    }

    /**
     * Returns the ID of this log event.
     *
     * @return ID of this log event
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the time when this log event happened.
     *
     * @return time when this log event happened
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the type of this log event.
     *
     * @return type of this log event
     */
    public EventType getType() {
        return type;
    }

    /**
     * Returns the source of this log event.
     *
     * @return source of this log event
     */
    public Object getSource() {
        return source;
    }

    /**
     * Represent different type of events.
     */
    public enum EventType {
        /**
         * Start of dispatching a HTTP request.
         */
        START_REQUEST,
        /**
         * Start of rendering a page.
         */
        START_PAGE,
        /**
         * Start of rendering a fragment.
         */
        START_FRAGMENT,
        /**
         * End of dispatching a HTTP request.
         */
        END_REQUEST,
        /**
         * End of rendering a page.
         */
        END_PAGE,
        /**
         * End of rendering a fragment.
         */
        END_FRAGMENT
    }
}
