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

import org.apache.commons.io.FileUtils;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.api.config.Configuration;
import org.wso2.carbon.uuf.exception.SessionManagerException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.cache.expiry.Duration;

/**
 * Manages sessions by storing serialized sessions to files.
 * <p>
 * This session manager will store the serialized sessions to files. The created session files
 * could be found in the system temporary directory
 * </p>
 * <p>
 * Please make note to specify the session manager class name in the app.yaml configuration file under the
 * 'sessionManager' key in order for this session manager to be used in the application.
 * </p>
 * <p>
 * eg: sessionManager: "org.wso2.carbon.uuf.sample.simpleauth.bundle.api.auth.PersistentSessionManager"
 * </p>
 * <p>
 * The session time-out duration (in seconds) can be specified in the <tt>component.yaml</tt> configuration file under
 * the 'sessionTimeoutDuration' key. This will make sure that the session file will be deleted in specified number of
 * seconds.
 * </p>
 * <p>
 * eg: sessionTimeoutDuration: 60 # This will keep the session file for 60 seconds
 * </p>
 *
 * @since 1.0.0
 */
@Component(name = "org.wso2.carbon.uuf.sample.simpleauth.bundle.api.auth.PersistentSessionManager",
        service = SessionManager.class,
        immediate = true
)
public class PersistentSessionManager implements SessionManager {

    private static final String SESSION_DIR_PREFIX = "sessions-";
    private static final String SESSION_TIME_OUT = "sessionTimeoutDuration";
    private static final int DEFAULT_SESSION_TIMEOUT_DURATION = 20 * 60;
    private static final String COOKIE_NAME_SESSION_ID = "UUFSESSIONID";
    private static final String COOKIE_NAME_CSRF_TOKEN = "CSRFTOKEN";

    private final Map<File, ScheduledFuture> futures = new HashMap<>();
    private final ScheduledExecutorService executor = Executors
            .newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    private final Path tempDirectory;

    public PersistentSessionManager() {
        try {
            tempDirectory = Files.createTempDirectory(SESSION_DIR_PREFIX).toAbsolutePath();
            tempDirectory.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new SessionManagerException("Error occurred when creating the temp directory " +
                    SESSION_DIR_PREFIX, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session createSession(User user, HttpRequest request, HttpResponse response, Configuration configuration)
            throws SessionManagerException {
        Session session = new Session(user);
        saveSession(session);
        // Create cookies
        response.addCookie(COOKIE_NAME_SESSION_ID, session.getSessionId() +
                "; Path=" + request.getContextPath() + "; Secure; HTTPOnly");
        response.addCookie(COOKIE_NAME_CSRF_TOKEN, session.getCsrfToken() + "; Path=" +
                request.getContextPath() + "; Secure");

        scheduleForDeletion(session, configuration);
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Session> getSession(HttpRequest request, HttpResponse response, Configuration configuration)
            throws SessionManagerException {
        String sessionId = request.getCookieValue(COOKIE_NAME_SESSION_ID);
        if (sessionId == null) {
            return Optional.empty();
        }
        if (!Session.isValidSessionId(sessionId)) {
            throw new IllegalArgumentException("Session ID '" + sessionId + "' is invalid.");
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
            throw new SessionManagerException("Cannot read session " + sessionId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroySession(HttpRequest request, HttpResponse response, Configuration configuration)
            throws SessionManagerException {
        String sessionId = request.getCookieValue(COOKIE_NAME_SESSION_ID);
        if (sessionId == null) {
            return true;
        }
        if (!Session.isValidSessionId(sessionId)) {
            throw new SessionManagerException("Session ID '" + sessionId + "' is invalid.");
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
        response.addCookie(COOKIE_NAME_SESSION_ID, expiredCookie);
        response.addCookie(COOKIE_NAME_CSRF_TOKEN, expiredCookie);
        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        File directory = tempDirectory.toFile();
        try {
            FileUtils.cleanDirectory(directory);
        } catch (IOException e) {
            throw new SessionManagerException("Error in deleting sessions in path " + directory, e);
        }
        futures.forEach((key, value) -> value.cancel(true));
        futures.clear();
        executor.shutdown();
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
            throw new SessionManagerException("Cannot save session " + session.getSessionId(), e);
        }
    }

    /**
     * Schedule a session file to be deleted.
     *
     * @param session session instance to be deleted
     */
    private void scheduleForDeletion(Session session, Configuration configuration) {
        Duration sessionTimeout = getSessionTimeOutDuration(configuration);
        File sessionFile = Paths.get(tempDirectory.toString(), session.getSessionId()).toFile();
        ScheduledFuture future = executor.schedule(sessionFile::delete,
                sessionTimeout.getDurationAmount(), sessionTimeout.getTimeUnit());
        futures.put(sessionFile, future);
    }

    /**
     * Returns the session time-out duration from the configuration.
     *
     * @param configuration app configuration
     * @return session time-out duration
     */
    private Duration getSessionTimeOutDuration(Configuration configuration) {
        Map<String, Object> otherConfigurations = configuration.other();
        int sessionTimeoutDuration = DEFAULT_SESSION_TIMEOUT_DURATION;
        if (otherConfigurations != null) {
            sessionTimeoutDuration = (int) otherConfigurations.getOrDefault(SESSION_TIME_OUT,
                    DEFAULT_SESSION_TIMEOUT_DURATION);
        }
        return new Duration(TimeUnit.SECONDS, sessionTimeoutDuration);
    }
}
