package org.wso2.carbon.uuf;

import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.MimeMapper;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.create.AppCreator;
import org.wso2.carbon.uuf.core.create.Resolver;
import org.wso2.msf4j.util.SystemVariableUtil;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.FileNameMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.wso2.carbon.uuf.core.create.Resolver.STATIC_RESOURCE_URI_PREFIX;

public class UUFRegistry {

    private static final Logger log = LoggerFactory.getLogger(UUFRegistry.class);
    private final AppCreator appCreator;
    private final Optional<DebugAppender> debugAppender;
    private final Map<String, App> apps = new HashMap<>();
    private Resolver resolver;
    private FileNameMap fileNameMap;
    private static final String CACHE_HEADER_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    public UUFRegistry(AppCreator appCreator, Optional<DebugAppender> debugAppender, Resolver resolver) {
        this.appCreator = appCreator;
        this.debugAppender = debugAppender;
        this.resolver = resolver;
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

    public Response.ResponseBuilder serve(HttpRequest request) {
        String hostHeader = request.headers().get("Host");
        String host = "//" + (hostHeader != null ? hostHeader : "localhost");
        String uri = request.getUri().replaceAll("/+", "/");
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
        String appContext = '/' + appName;
        String resourcePath = uri.substring(firstSlash, uri.length());

        if (log.isDebugEnabled() && !resourcePath.startsWith("/debug/")) {
            log.debug("request received " + request.getMethod() + " " + request.getUri() + " " + request
                    .getProtocolVersion());
        }

        App app = apps.get(appName);
        try {
            if (isStaticResourceRequest(resourcePath)) {
                Path resource = resolver.resolveStatic(appName, resourcePath);
                if (Files.exists(resource) && Files.isRegularFile(resource)) {
                    return getCachedResponseBuilder(resource, request);
                } else {
                    return Response.status(Response.Status.NOT_FOUND).entity(
                            "Requested resource '" + uri + "' does not exists at '" + resource + "'");
                }
            } else {
                if (app == null || debugAppender.isPresent()) {
                    app = appCreator.createApp(appName, appContext);
                    apps.put(appName, app);
                }
                if (resourcePath.equals("/debug/api/pages/")) {
                    //TODO: fix issues when same page is in multiple components
                    return Response.ok(app
                            .getComponents()
                            .entrySet()
                            .stream()
                            .flatMap(entry -> entry.getValue().getPages().stream())
                            .collect(Collectors.toSet()));
                }

//TODO:fix
//                if (resourcePath.startsWith("/debug/api/fragments/")) {
//                    return Response.ok(app
//                            .getComponents()
//                            .entrySet()
//                            .stream()
//                            .flatMap(entry -> entry.getValue().getFragments().values().stream())
//                            .collect(Collectors.toSet()));
//                }
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
                    }
                    InputStream resourceAsStream = this.getClass().getResourceAsStream("/apps" + resourcePath);
                    if (resourceAsStream != null) {
                        String debugContent = IOUtils.toString(
                                resourceAsStream,
                                "UTF-8");
                        return Response.ok(debugContent, getMime(resourcePath));
                    } else {
                        return Response.status(Response.Status.NOT_FOUND);
                    }
                }
                String page = app.renderPage(appContext, uri.substring(appContext.length()));
                return Response.ok(page).header("Content-Type", "text/html");
            }
            //TODO: Don't catch this Ex, move the logic below the 'instanceof' check
        } catch (UUFException e) {

            // https://googlewebmastercentral.blogspot.com/2010/04/to-slash-or-not-to-slash.html
            // if the tailing / is extra or a it is missing, send 301
            if (e.getStatus() == Response.Status.NOT_FOUND && app != null) {
                if (uri.endsWith("/")) {
                    String uriWithoutSlash = resourcePath.substring(0, resourcePath.length() - 1);
                    if (app.hasPage(uriWithoutSlash)) {
                        return Response.status(301).header("Location", host + uriWithoutSlash);
                    }
                } else {
                    String uriWithSlash = resourcePath + "/";
                    if (app.hasPage(uriWithSlash)) {
                        return Response.status(301).header("Location", host + uri + "/");
                    }
                }
            }

            return sendError(appName, e, e.getStatus());
        } catch (Exception e) {
            Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
            Throwable cause = e.getCause();
            //TODO check this loop's logic
            while (cause != null) {
                if (cause instanceof UUFException) {
                    status = ((UUFException) cause).getStatus();
                    break;
                }
                if (cause == cause.getCause()) {
                    break;
                }
                cause = cause.getCause();
            }
            return sendError(appName, e, status);
        }
    }

    private String getMime(String resourcePath) {
        int extensionIndex = resourcePath.lastIndexOf(".");
        String extension = (extensionIndex == -1) ? resourcePath : resourcePath.substring(extensionIndex + 1,
                resourcePath.length());
        Optional<String> mime = MimeMapper.getMimeType(extension);
        return (mime.isPresent()) ? mime.get() : "text/html";
    }

    private Response.ResponseBuilder getCachedResponseBuilder(Path resource, HttpRequest request) throws IOException{
        Optional<Date> ifModDate = getIfModifiedSinceDate(request);
        BasicFileAttributes attrs = Files.readAttributes(resource, BasicFileAttributes.class);
        Date resourceModDate = new Date(attrs.lastModifiedTime().toMillis());
        if (ifModDate.isPresent() && (!ifModDate.get().after(resourceModDate))) {//!after = before || equal
            return Response.notModified();
        }
        return setCacheHeaders(resourceModDate, Response.ok(resource.toFile(), getMime(resource.toString())));
    }

    private Optional<Date> getIfModifiedSinceDate(HttpRequest request) {
        String httpDateStr = request.headers().get("If-Modified-Since");
        if(httpDateStr == null){
            return Optional.empty();
        }
        SimpleDateFormat df = new SimpleDateFormat(CACHE_HEADER_DATE_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            return Optional.of(df.parse(httpDateStr));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    private Response.ResponseBuilder setCacheHeaders(Date lastModDate, Response.ResponseBuilder builder) {
        //currently MSF4J does not implement cacheControl.
        //hence cache control headers are set manually'
        SimpleDateFormat df = new SimpleDateFormat(CACHE_HEADER_DATE_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        builder.header("Last-Modified", df.format(lastModDate));
        builder.header("Cache-Control", "public,max-age=2592000");
        return builder;
    }

    private boolean isStaticResourceRequest(String resourcePath) {
        return resourcePath.startsWith("/" + STATIC_RESOURCE_URI_PREFIX);
    }

    private Response.ResponseBuilder sendError(String appName, Exception e, Response.Status status) {
        log.error("error while serving context '" + appName + "'", e);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return Response.status(status).entity(sw.toString()).header("Content-Type", "text/plain");
    }
}
