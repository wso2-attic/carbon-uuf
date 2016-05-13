/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.uuf.connector;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.wso2.carbon.uuf.api.HttpRequest;
import org.wso2.carbon.uuf.internal.UUFRegistry;
import org.wso2.carbon.uuf.internal.core.create.AppCreator;
import org.wso2.carbon.uuf.internal.core.create.AppDiscoverer;
import org.wso2.carbon.uuf.internal.io.ArtifactAppDiscoverer;
import org.wso2.carbon.uuf.internal.io.BundleClassLoaderProvider;
import org.wso2.carbon.uuf.internal.io.StaticResolver;
import org.wso2.carbon.uuf.spi.RenderableCreator;
import org.wso2.msf4j.HttpResponder;
import org.wso2.msf4j.HttpStreamHandler;
import org.wso2.msf4j.HttpStreamer;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * UUF Connector for MSF4J.
 */
@Component(name = "org.wso2.carbon.uuf.connector.MicroserviceConnector",
           service = Microservice.class,
           immediate = true)
@Path("/")
public class MicroserviceConnector implements Microservice {

    private static final Set<RenderableCreator> RENDERABLE_CREATORS = new HashSet<>();
    private static final Logger log = LoggerFactory.getLogger(MicroserviceConnector.class);
    private UUFRegistry registry;
    private final AtomicInteger count = new AtomicInteger(0);

    @SuppressWarnings("unused")
    public MicroserviceConnector() {
        // We need an empty constructor for running in OSGi mode.
        this(createRegistry());
    }

    public MicroserviceConnector(UUFRegistry server) {
        // Used in the fat-jar mode.
        this.registry = server;
    }

    private static UUFRegistry createRegistry() {
        AppDiscoverer appDiscoverer = new ArtifactAppDiscoverer();
        AppCreator appCreator = new AppCreator(RENDERABLE_CREATORS, new BundleClassLoaderProvider());
        StaticResolver staticResolver = new StaticResolver();
        return new UUFRegistry(appDiscoverer, appCreator, staticResolver);
    }

    @GET
    @Path(".*")
    @Produces({"text/plain"})
    public Response get(@Context io.netty.handler.codec.http.HttpRequest request) {
        return execute(new MicroserviceHttpRequest(request, null));
    }

    @POST
    @Path(".*")
    @Produces({"text/plain"})
    public void post(@Context HttpStreamer httpStreamer, @Context io.netty.handler.codec.http.HttpRequest request) {
        httpStreamer.callback(new HttpStreamHandlerImpl(this, request));
    }

    private Response execute(HttpRequest request) {
        try {
            MDC.put("uuf-request", String.valueOf(count.incrementAndGet()));
            Response.ResponseBuilder response = registry.serve(request);
            return response.build();
        } finally {
            try {
                MDC.remove("uuf-request");
            } catch (Exception ex) {
                //ignore, just catching so ide wan't complain. MDC will never throw an IllegalArgumentException.
            }
        }
    }

    /**
     * This bind method is invoked by OSGi framework whenever a new RenderableCreator is registered.
     *
     * @param renderableCreator registered renderable creator
     */
    @Reference(name = "renderablecreater",
               service = RenderableCreator.class,
               cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetRenderableCreator")
    @SuppressWarnings("unused")
    protected void setRenderableCreator(RenderableCreator renderableCreator) {
        if (!RENDERABLE_CREATORS.add(renderableCreator)) {
            throw new IllegalArgumentException(
                    "A RenderableCreator for '" + renderableCreator.getSupportedFileExtensions() +
                            "' extensions is already registered");
        }
        this.registry = createRegistry();
    }

    /**
     * This bind method is invoked by OSGi framework whenever a RenderableCreator is left.
     *
     * @param renderableCreator unregistered renderable creator
     */
    @SuppressWarnings("unused")
    protected void unsetRenderableCreator(RenderableCreator renderableCreator) {
        RENDERABLE_CREATORS.remove(renderableCreator);
        this.registry = createRegistry();
    }

    private static class HttpStreamHandlerImpl implements HttpStreamHandler {
        private final ByteArrayOutputStream content = new ByteArrayOutputStream();
        private final MicroserviceConnector microserviceConnector;
        private final io.netty.handler.codec.http.HttpRequest nettyRequest;

        public HttpStreamHandlerImpl(MicroserviceConnector microserviceConnector,
                                     io.netty.handler.codec.http.HttpRequest nettyRequest) {
            this.microserviceConnector = microserviceConnector;
            this.nettyRequest = nettyRequest;
        }

        @Override
        public void chunk(ByteBuf request, HttpResponder responder) throws IOException {
            request.readBytes(content, request.capacity());
        }

        @Override
        public void finished(ByteBuf request, HttpResponder responder) throws IOException {
            request.readBytes(content, request.capacity());
            content.close();

            HttpRequest httpRequest = new MicroserviceHttpRequest(this.nettyRequest, content.toByteArray());
            Response response = microserviceConnector.execute(httpRequest);
            ByteBuf channelBuffer = Unpooled.wrappedBuffer(response.getEntity().toString().getBytes());
            Multimap<String, String> headers = ArrayListMultimap.create();
            response.getHeaders().forEach(
                    (hKey, hList) -> hList.forEach(hValue -> headers.put(hKey, hValue.toString())));
            HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(response.getStatus());
            if (response.hasEntity()) {
                responder.sendContent(httpResponseStatus, channelBuffer,
                                      response.getHeaders().get(HttpHeaders.CONTENT_TYPE).toString(), headers);
            } else {
                responder.sendStatus(httpResponseStatus);
            }
        }

        @Override
        public void error(Throwable cause) {
            try {
                content.close();
            } catch (IOException e) {
                // Log if unable to close the output stream
                log.error("Unable to close byte array output stream", e);
            }
        }
    }
}
