package org.wso2.carbon.uuf;

import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.AppCreator;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.fileio.FromArtifactAppCreator;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.util.SystemVariableUtil;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UUFRegistry {

    private static final Logger log = LoggerFactory.getLogger(UUFRegistry.class);
    private final AppCreator appCreator;
    private Optional<DebugAppender> debugAppender;
    private final Map<String, App> apps = new HashMap<>();

    public UUFRegistry(AppCreator appCreator, Optional<DebugAppender> debugAppender) {
        this.appCreator = appCreator;
        this.debugAppender = debugAppender;
    }

    public static Optional<DebugAppender> createDebugAppender() {
        String uufDebug = SystemVariableUtil.getValue("uufDebug", "false");
        if (uufDebug.equalsIgnoreCase("true")) {
            DebugAppender appender = new DebugAppender();
            appender.attach();
            return Optional.of(appender);
        } else {
            return Optional.empty();
        }
    }

    public static void main(String[] args) {
        UUFRegistry registry = new UUFRegistry(new FromArtifactAppCreator(new String[]{"."}), createDebugAppender());
        new MicroservicesRunner().deploy(new UUFService(registry)).start();
    }

    public Response.ResponseBuilder serve(HttpRequest request) {
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
        String resourcePath = uri.substring(firstSlash, uri.length());

        if (log.isDebugEnabled() && !resourcePath.startsWith("/debug/")) {
            log.debug("request received " + request.getMethod() + " " + request.getUri() + " " + request
                    .getProtocolVersion());
        }

        App app = apps.get(appName);
        try {
            String type = URLConnection.guessContentTypeFromName(resourcePath);
            if (type == null) {
                type = "application/octet-stream";
            }
            if (isStaticResourceRequest(resourcePath)) {
                Path resource = appCreator.resolve(appName, resourcePath);
                if (Files.exists(resource) && Files.isRegularFile(resource)) {
                    return Response.ok(resource.toFile(),
                            type);
                } else {
                    return Response.status(Response.Status.NOT_FOUND).entity(
                            "Requested resource `" + uri + "` does not exists!");
                }
            } else {
                if (app == null || debugAppender.isPresent()) {
                    app = appCreator.createApp(appName, "/" + appName);
                    apps.put(appName, app);
                }
                if (resourcePath.startsWith("/debug/api/")) {
                    return Response.ok(app.getPages().toString(), "application/json");
                }
                if (resourcePath.startsWith("/debug/logs")) {
                    if (debugAppender.isPresent()) {
                        return Response.ok(debugAppender.get().asJson(), "application/json");
                    } else {
                        return Response.status(Response.Status.GONE);
                    }
                }
                if (resourcePath.startsWith("/debug/")) {
                    if (resourcePath.endsWith("/")) {
                        resourcePath = resourcePath + "index.html";
                        type = URLConnection.guessContentTypeFromName(resourcePath);
                    }
                    InputStream resourceAsStream = this.getClass().getResourceAsStream("/apps" + resourcePath);
                    if (resourceAsStream != null) {
                        String debugContent = IOUtils.toString(
                                resourceAsStream,
                                "UTF-8");
                        return Response.ok(debugContent, type);
                    } else {
                        return Response.status(Response.Status.NOT_FOUND);
                    }
                }
                String page = app.renderPage(request);
                return Response.ok(page).header("Content-Type", "text/html");
            }
            //TODO: Don't catch this Ex, move the logic below the 'instanceof' check
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
                if (cause == e.getCause()) {
                    break;
                }
                cause = e.getCause();
            }
            return sendError(appName, e, status);
        }
    }

    private boolean isStaticResourceRequest(String resourcePath) {
        return resourcePath.startsWith("/" + AppCreator.STATIC_RESOURCE_PREFIX);
    }

    private Response.ResponseBuilder sendError(String appName, Exception e, Response.Status status) {
        log.error("error while serving context '" + appName + "'", e);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return Response.status(status).entity(sw.toString()).header("Content-Type", "text/plain");
    }
}
