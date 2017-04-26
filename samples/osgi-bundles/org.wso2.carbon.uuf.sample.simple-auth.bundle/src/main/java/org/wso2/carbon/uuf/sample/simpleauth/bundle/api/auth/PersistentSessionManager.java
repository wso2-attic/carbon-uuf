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

package org.wso2.carbon.uuf.sample.simpleauth.bundle.api.auth;

import org.apache.commons.io.FileUtils;
import org.wso2.carbon.uuf.api.auth.Session;
import org.wso2.carbon.uuf.api.config.Configuration;
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
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Sample persistent session manager.
 */
public class PersistentSessionManager implements SessionManager {

    private static final String SESSION_DIR = ".sessions";

    @Override
    public void init(Configuration configuration) {
        File dir = new File(SESSION_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create .tmp directory");
        }
    }

    @Override
    public void clear() {
        String path = Paths.get(SESSION_DIR).toString();
        File directory = new File(path);
        try {
            FileUtils.cleanDirectory(directory);
        } catch (IOException e) {
            throw new RuntimeException("Error in deleting sessions in path " + path, e);
        }
    }

    @Override
    public int getCount() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public void close() {
        clear();
    }

    @Override
    public Session createSession(User user, HttpRequest request, HttpResponse response) {
        Session session = new Session(user);
        saveSession(session);
        return session;
    }

    @Override
    public Optional<Session> getSession(HttpRequest request, HttpResponse response) {
        String sessionId = request.getCookieValue(Session.SESSION_COOKIE_NAME);
        String path = Paths.get(SESSION_DIR, sessionId).toString();
        if (!new File(path).exists()) {
            return Optional.empty();
        }
        try (FileInputStream fis = new FileInputStream(path);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Session session = (Session) ois.readObject();
            return Optional.ofNullable(session);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Cannot read session " + sessionId, e);
        }
    }

    @Override
    public boolean destroySession(HttpRequest request, HttpResponse response) {
        String sessionId = request.getCookieValue(Session.SESSION_COOKIE_NAME);
        String pathname = Paths.get(SESSION_DIR, sessionId).toString();
        return new File(pathname).delete();
    }

    private void saveSession(Session session) {
        try (FileOutputStream fout = new FileOutputStream(Paths.get(SESSION_DIR, session.getSessionId()).toString());
             ObjectOutputStream oos = new ObjectOutputStream(fout)) {
            oos.writeObject(session);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save session " + session.getSessionId(), e);
        }
    }
}
