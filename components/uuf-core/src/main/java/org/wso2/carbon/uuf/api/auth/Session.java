/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.api.auth;

import org.wso2.carbon.uuf.core.Theme;
import org.wso2.carbon.uuf.spi.auth.User;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Provides a way to identify a user across more than one page request or visit to a Web site and to store information
 * about that user.
 * <p>
 * The {@link org.wso2.carbon.uuf.internal.auth.SessionRegistry SessionRegistry} uses this class to create a session
 * between an HTTP client and an HTTP server. The session persists for a specified time period, across more than one
 * connection or page request from the user.
 */
public class Session implements Serializable {

    /**
     * Number of bytes in a session ID.
     */
    public static final int SESSION_ID_LENGTH = 16;
    private static final SessionIdGenerator sessionIdGenerator = new SessionIdGenerator(SESSION_ID_LENGTH);

    private final String sessionId;
    private final User user;
    private String themeName;

    public Session(User user) {
        this.sessionId = sessionIdGenerator.generateId();
        this.user = user;
    }

    public String getSessionId() {
        return sessionId;
    }

    public User getUser() {
        return user;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        if (!Theme.isValidThemeName(themeName)) {
            throw new IllegalArgumentException("Theme name '" + themeName + "' is invalid.");
        }
        this.themeName = themeName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Session) && (sessionId.equals(((Session) obj).sessionId));
    }

    @Override
    public String toString() {
        return "{\"sessionId\": \"" + sessionId + "\", \"user\": \"" + user + "\", \"theme\": \"" + themeName + "\"}";
    }

    public static boolean isValidSessionId(String sessionId) {
        return (sessionId != null) && !sessionId.isEmpty() && (sessionId.length() == Session.SESSION_ID_LENGTH * 2);
    }

    /**
     * Adopted from <a href="https://git.io/vrYMl">org.apache.catalina.util.SessionIdGenerator</a> in Apache Tomcat
     * 8.0.0 release.
     */
    private static class SessionIdGenerator {

        private final SecureRandom secureRandom;
        private final int sessionIdLength;

        /**
         * Creates a new session ID generator.
         *
         * @param sessionIdLength number of bytes in a session ID
         */
        public SessionIdGenerator(int sessionIdLength) {
            byte[] randomBytes = new byte[32];
            ThreadLocalRandom.current().nextBytes(randomBytes);
            char[] entropy = Base64.getEncoder().encodeToString(randomBytes).toCharArray();

            long seed = System.currentTimeMillis();
            for (int i = 0; i < entropy.length; i++) {
                long update = ((byte) entropy[i]) << ((i % 8) * 8);
                seed ^= update;
            }

            // We call the default constructor so that system will figure-out the best, available algorithm.
            // See: http://stackoverflow.com/a/27638413/1577286
            this.secureRandom = new SecureRandom();
            this.secureRandom.setSeed(seed);
            this.sessionIdLength = sessionIdLength;
        }

        public synchronized String generateId() {
            byte randomBytes[] = new byte[16];
            // Render the result as a String of hexadecimal digits
            StringBuilder buffer = new StringBuilder();

            int resultLenBytes = 0;

            while (resultLenBytes < sessionIdLength) {
                secureRandom.nextBytes(randomBytes);
                for (int j = 0; j < randomBytes.length && resultLenBytes < sessionIdLength; j++) {
                    byte b1 = (byte) ((randomBytes[j] & 0xf0) >> 4);
                    byte b2 = (byte) (randomBytes[j] & 0x0f);
                    if (b1 < 10) {
                        buffer.append((char) ('0' + b1));
                    } else {
                        buffer.append((char) ('A' + (b1 - 10)));
                    }
                    if (b2 < 10) {
                        buffer.append((char) ('0' + b2));
                    } else {
                        buffer.append((char) ('A' + (b2 - 10)));
                    }
                    resultLenBytes++;
                }
            }
            return buffer.toString();
        }
    }
}
