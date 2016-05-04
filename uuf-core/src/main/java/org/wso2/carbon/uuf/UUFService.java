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

package org.wso2.carbon.uuf;

import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
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
import org.wso2.carbon.uuf.fileio.StaticResolver;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
/**
 * UUF Main Service.
 */
@Component(name = "org.wso2.carbon.uuf.UUFService",
           service = Microservice.class,
           immediate = true)
@Path("/")
public class UUFService implements Microservice {

    private static final Set<RenderableCreator> RENDERABLE_CREATORS = new HashSet<>();

    private UUFRegistry registry;
    private final AtomicInteger count = new AtomicInteger(0);

    @SuppressWarnings("unused")
    public UUFService() {
        // We need an empty constructor for running in OSGi mode.
        this(createRegistry());
    }

    public UUFService(UUFRegistry server) {
        // Used in the fat-jar mode.
        this.registry = server;
    }

    private static UUFRegistry createRegistry() {
        ArtifactResolver appResolver = new ArtifactResolver();
        StaticResolver staticResolver = new StaticResolver();
        AppCreator appCreator = new AppCreator(RENDERABLE_CREATORS, new BundleClassLoaderProvider());
        return new UUFRegistry(appCreator, Optional.empty(), appResolver, staticResolver);
    }

    @GET
    @Path(".*")
    @Produces({"text/plain"})
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
}
