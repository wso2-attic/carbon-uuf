## Pluggable Session Management

This bundle uses a custom implementation of the [SessionManager.java](../../../components/uuf-core/src/main/java/org/wso2/carbon/uuf/spi/auth/SessionManager.java).

### How to plug in a custom session manager

**Step 1:**

Write your custom implementation of the session manager, implementing
the [SessionManager.java](../../../components/uuf-core/src/main/java/org/wso2/carbon/uuf/spi/auth/SessionManager.java) 
interface. The session manager implemented in this sample is as shown below:

```java
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

```

**Step 2:**

Specify the "sessionManager:" entry in the relavent `app.yaml` configuration.
Shown below is a sample `app.yaml` used in the [pets-store app](../../apps/org.wso2.carbon.uuf.sample.pets-store.app/src/main/app.yaml).

```yaml
# Configurations of Pets Store app.
...

# Session manager for the app
sessionManager: "org.wso2.carbon.uuf.sample.simpleauth.bundle.api.auth.PersistentSessionManager"

...
```
Now the `PersistentSessionManager.java` will be used as the apps session manager.

### How to test the `PersistentSessionManager.java`

**Step 1:**

Navigate to [UUF product](../../../product/) and execute the command below:

```bash
mvn clean install
```

This will build the UUF product. 

**Step 2:**

Navigate the [UUF product target folder](../../../product/target) 
and unzip the wso2uuf-{version}.zip

Go to the extracted zip folder (lets consider this location as "UUF-HOME")

**Step 3:**

Go to <UUF-HOME>/bin and execute the following command.

```bash
sh carbon.sh
```

This will start the UUF server.

**Step 4:**

Using your web browser, go to `https://localhost:9292/pets-store`.

You can login to the pet-store app by using the following credentials:

* **Username:** admin
* **Password:** admin

According to the `PersistentSessionManager.java`'s session management implementation,
your sessions will be created at `<UUF-HOME>/.sessions`

You can delete the session file and see if you would be re-directed to the login
page upon refreshing the browser.