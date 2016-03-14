package org.wso2.carbon.uuf;

import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.AppCreator;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.fileio.FromArtifactAppCreator;
import org.wso2.msf4j.MicroservicesRunner;

import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class UUFRegistry {

    private static final Logger log = LoggerFactory.getLogger(UUFRegistry.class);
    private final AppCreator appCreator;
    private final Map<String, App> apps = new HashMap<>();

    public UUFRegistry(AppCreator appCreator) {
        this.appCreator = appCreator;
    }

    public static void main(String[] args) {
        //TODO: only if debug is enabled
        DebugAppender.attach();
        UUFRegistry registry = new UUFRegistry(new FromArtifactAppCreator(new String[]{"."}));
        new MicroservicesRunner().deploy(new UUFService(registry)).start();
    }

    public Response.ResponseBuilder serve(HttpRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("request received " + request.getMethod() + " "
                    + request.getUri() + " " + request.getProtocolVersion());
        }

        String uri = request.getUri();
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }

        int firstSlash = uri.indexOf('/', 1);

        if (firstSlash < 0) {
            if (uri.equals("/favicon.ico")) {
                //TODO: send a favicon, cacheable favicon avoids frequent requests for it.
                return Response.status(404).entity("");
            }

            // eg: url = http://example.com/app and uri = /app
            // since we don't support ROOT app, this must be a mis-type
            return Response.status(301).entity("").header("Location", uri + "/");
        }

        String appName = uri.substring(1, firstSlash);

        App app = apps.get(appName);
        try {
            if (app == null) {
                app = appCreator.createApp(appName, "/" + appName);
                apps.put(appName, app);
            }
            String page = app.serve(request);
            return Response.ok().entity(page).header("Content-Type", "text/html");
        } catch (UUFException e) {


            // https://googlewebmastercentral.blogspot.com/2010/04/to-slash-or-not-to-slash.html
            // if the tailing / is extra or a it is missing, send 301
            if (e.getStatus() == Response.Status.NOT_FOUND && app != null) {
                if (uri.endsWith("/")) {
                    String uriNoSlash = uri.substring(0, uri.length() - 1);
                    if (app.getPage(uriNoSlash) != null) {
                        return Response.status(301).header("Location", uriNoSlash);
                    }
                } else {
                    if (app.getPage(uri + "/") != null) {
                        return Response.status(301).header("Location", uri + "/");
                    }
                }
            }

            return sendError(appName, e, e.getStatus());
        } catch (Exception e) {
            Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause instanceof UUFException) {
                    status = ((UUFException) cause).getStatus();
                    break;
                }
                cause = e.getCause();
            }
            return sendError(appName, e, status);
        }
    }

    private Response.ResponseBuilder sendError(String appName, Exception e, Response.Status status) {
        log.error("error while serving context '" + appName + "'", e);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return Response.status(status)
                .entity(sw.toString())
                .header("Content-Type", "text/plain");
    }
}
