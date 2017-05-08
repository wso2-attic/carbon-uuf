/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.uuf.sample.simpleauth.bundle.api.auth;

import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.exception.SessionManagementException;
import org.wso2.carbon.uuf.spi.HttpRequest;
import org.wso2.carbon.uuf.spi.HttpResponse;
import org.wso2.carbon.uuf.spi.auth.SessionManager;
import org.wso2.carbon.uuf.spi.auth.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages sessions for a single UUF app by storing serialized sessions to files.
 * <p>
 * This session manager will store the serialized sessions to files. The created session files
 * could be found in the system temporary directory
 *
 * @since 1.0.0
 */
public class PersistentSessionManager implements SessionManager {

    private static final long SESSION_DEFAULT_TIMEOUT = 1200L; // 20 minutes
    private static final String SESSION_DIR_PREFIX = "sessions-";
    private static final String COOKIE_SESSION_ID = "UUFSESSIONID";
    private static final String COOKIE_CSRF_TOKEN = "CSRFTOKEN";

    private final Map<File, ScheduledFuture> futures = new HashMap<>();
    private final ScheduledExecutorService executor = Executors
            .newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    private final Path tempDirectory;
    private final Configuration configuration;

    /**
     * Constructs a new PersistentSessionManager.
     *
     * @param appName       name of the UUF application (or app context)
     * @param configuration app configuration
     */
    public PersistentSessionManager(String appName, Configuration configuration) {
        this.configuration = configuration;
        try {
            tempDirectory = Files.createTempDirectory(SESSION_DIR_PREFIX).toAbsolutePath();
            tempDirectory.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new SessionManagementException("Error occurred when creating the temp directory " +
                    SESSION_DIR_PREFIX, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        throw new SessionManagementException("This operation is not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session createSession(User user, HttpRequest request, HttpResponse response)
            throws SessionManagementException {
        Session session = new Session(user);
        saveSession(session);
        // Create cookies
        response.addCookie(COOKIE_SESSION_ID, session.getSessionId() +
                "; Path=" + request.getContextPath() + "; Secure; HTTPOnly");
        response.addCookie(COOKIE_CSRF_TOKEN, session.getCsrfToken() + "; Path=" +
                request.getContextPath() + "; Secure");
        scheduleForDeletion(session, configuration);
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Session> getSession(HttpRequest request, HttpResponse response) throws SessionManagementException {
        String sessionId = request.getCookieValue(COOKIE_SESSION_ID);
        if (sessionId == null) {
            return Optional.empty();
        }
        if (!Session.isValidSessionId(sessionId)) {
            throw new SessionManagementException("Session ID '" + sessionId + "' is invalid.");
        }
        File sessionFile = Paths.get(tempDirectory.toString(), sessionId).toFile();
        if (!sessionFile.exists()) {
            return Optional.empty();
        }
        try (FileInputStream fis = new FileInputStream(sessionFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Session session = (Session) ois.readObject();
            // Reschedule deletion
            ScheduledFuture future = futures.get(sessionFile);
            if (future != null) {
                future.cancel(true);
                futures.remove(sessionFile);
            }
            scheduleForDeletion(session, configuration);
            return Optional.ofNullable(session);
        } catch (IOException | ClassNotFoundException e) {
            throw new SessionManagementException("Cannot read session " + sessionId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroySession(HttpRequest request, HttpResponse response)
            throws SessionManagementException {
        String sessionId = request.getCookieValue(COOKIE_SESSION_ID);
        if (sessionId == null) {
            return true;
        }
        if (!Session.isValidSessionId(sessionId)) {
            throw new SessionManagementException("Session ID '" + sessionId + "' is invalid.");
        }
        File sessionFile = Paths.get(tempDirectory.toString(), sessionId).toFile();
        boolean deleted = sessionFile.delete();
        ScheduledFuture future = futures.get(sessionFile);
        if (future != null) {
            future.cancel(true);
            futures.remove(sessionFile);
        }
        // Clear the session cookie by setting its value to an empty string, Max-Age to zero, & Expires to a past date.
        String expiredCookie = "Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:01 GMT; Path=" + request.getContextPath() +
                "; Secure; HTTPOnly";
        response.addCookie(COOKIE_SESSION_ID, expiredCookie);
        response.addCookie(COOKIE_CSRF_TOKEN, expiredCookie);
        return deleted;
    }

    /**
     * Save session to storage.
     *
     * @param session session to be persisted
     */
    private void saveSession(Session session) {
        File sessionFile = Paths.get(tempDirectory.toString(), session.getSessionId()).toFile();
        try (FileOutputStream fout = new FileOutputStream(sessionFile);
             ObjectOutputStream oos = new ObjectOutputStream(fout)) {
            oos.writeObject(session);
            sessionFile.deleteOnExit();
        } catch (IOException e) {
            throw new SessionManagementException("Cannot save session " + session.getSessionId(), e);
        }
    }

    /**
     * Schedule a session file to be deleted.
     * <p>
     * If the session timeout duration is not set in the configuration or if the session timeout duration value is 0
     * then the default session timeout duration of 20 minutes will be used to expire the session.
     *
     * @param session session to be deleted
     */
    private void scheduleForDeletion(Session session, Configuration configuration) {
        long sessionTimeout = configuration.getSessionTimeout();
        sessionTimeout = sessionTimeout == 0 ? SESSION_DEFAULT_TIMEOUT : sessionTimeout;
        Duration timeout = Duration.ofSeconds(sessionTimeout);
        File sessionFile = Paths.get(tempDirectory.toString(), session.getSessionId()).toFile();
        ScheduledFuture future = executor.schedule(sessionFile::delete, timeout.getSeconds(), TimeUnit.SECONDS);
        futures.put(sessionFile, future);
    }
}
