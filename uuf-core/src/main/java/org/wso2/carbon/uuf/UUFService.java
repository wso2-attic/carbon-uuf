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
import org.slf4j.MDC;
import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.uuf.fileio.FromArtifactAppCreator;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.wso2.carbon.uuf.UUFRegistry.createDebugAppender;

/**
 * UUF Main Service.
 */
@Component(
        name = "org.wso2.carbon.uuf.UUFService",
        service = Microservice.class,
        immediate = true
)
@Path("/")
public class UUFService implements Microservice {

    private final UUFRegistry registry;
    private final AtomicInteger count = new AtomicInteger(0);

    public UUFService(UUFRegistry server) {
        this.registry = server;
    }

    @SuppressWarnings("unused")
    public UUFService() throws IOException{
        // we need this constructor for running in OSGi mode.
        this(new UUFRegistry(new FromArtifactAppCreator(
                Files.list(Utils.getCarbonHome().resolve("deployment").resolve("uufapps"))
                        .collect(Collectors.toList()))));
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

}
