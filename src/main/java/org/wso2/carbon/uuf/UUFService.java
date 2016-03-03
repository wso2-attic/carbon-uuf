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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.util.SystemVariableUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.concurrent.atomic.AtomicInteger;

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

    private UUFRegistry registry;
    private AtomicInteger count = new AtomicInteger(0);

    public UUFService(UUFRegistry server) {
        this.registry = server;
    }

    public UUFService() {
        this(new UUFRegistry(new FileSystemAppFactory(
                SystemVariableUtil.getValue("uufApps", ".").split("\\s*,\\s*")
        )));
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
            MDC.remove("uuf-request");
        }
//        return Response.ok().entity(request.getUri() ).build();
    }

}
