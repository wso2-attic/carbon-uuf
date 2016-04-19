/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.uuf;

import io.netty.handler.codec.http.HttpRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.MDC;
import org.wso2.carbon.uuf.core.create.AppCreator;
import org.wso2.carbon.uuf.core.create.RenderableCreator;
import org.wso2.carbon.uuf.fileio.ArtifactResolver;
import org.wso2.carbon.uuf.fileio.BundleClassLoaderProvider;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * UUF Main Service.
 */
@Component(
        name = "org.wso2.carbon.uuf.UUFService",
        service = Microservice.class,
        immediate = true)
@Path("/")
public class UUFService implements Microservice {

    private UUFRegistry registry;
    private final AtomicInteger count = new AtomicInteger(0);
    private static final Map<String, RenderableCreator> creators = new ConcurrentHashMap<>();

    public UUFService(UUFRegistry server) {
        this.registry = server;
    }

    @SuppressWarnings("unused")
    public UUFService() {
        // we need an empty constructor for running in OSGi mode.
        this(createRegistry());
    }

    private static UUFRegistry createRegistry() {
        ArtifactResolver resolver = new ArtifactResolver();
        AppCreator appCreator = new AppCreator(creators, new BundleClassLoaderProvider());
        return new UUFRegistry(appCreator, Optional.empty(), resolver);
    }

    @GET
    @Path(".*")
    @Produces({ "text/plain" })
    public Response get(@Context HttpRequest request) {
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
     * @param renderableCreator registered renderable creator
     */
    @Reference(
            name = "renderablecreater",
            service = RenderableCreator.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRenderableCreator")
    @SuppressWarnings("unused")
    protected void setRenderableCreator(RenderableCreator renderableCreator) {
        renderableCreator.getSupportedFileExtensions().stream().forEach(
                key -> creators.put(key, renderableCreator));
        this.registry = createRegistry();
    }

    /**
     * This bind method is invoked by OSGi framework whenever a RenderableCreator is left.
     * @param renderableCreator unregistered renderable creator
     */
    @SuppressWarnings("unused")
    protected void unsetRenderableCreator(RenderableCreator renderableCreator) {
        renderableCreator.getSupportedFileExtensions().stream().forEach(creators::remove);
        this.registry = createRegistry();
    }

}
