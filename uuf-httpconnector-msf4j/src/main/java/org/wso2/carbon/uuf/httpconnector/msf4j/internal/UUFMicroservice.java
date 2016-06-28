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

package org.wso2.carbon.uuf.httpconnector.msf4j.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.ServerConnection;
import org.wso2.carbon.uuf.httpconnector.msf4j.MicroserviceHttpRequest;
import org.wso2.carbon.uuf.httpconnector.msf4j.MicroserviceHttpResponse;
import org.wso2.carbon.uuf.spi.HttpConnector;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FormParamIterator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * UUF Connector for MSF4J.
 */
@Component(name = "UUFMicroservice",
           service = {Microservice.class, HttpConnector.class},
           immediate = true)
@Path("/")
public class UUFMicroservice implements Microservice, HttpConnector {

    private static final Logger log = LoggerFactory.getLogger(UUFMicroservice.class);
    private ServerConnection serverConnection;

    @Activate
    protected void activate() {
        log.debug("UUFMicroservice activated.");
    }

    @Deactivate
    protected void deactivate() {
        log.debug("UUFMicroservice deactivated.");
    }

    @GET
    @Path(".*")
    public Response get(@Context Request request) {
        MicroserviceHttpRequest httpRequest = new MicroserviceHttpRequest(request);
        MicroserviceHttpResponse httpResponse = new MicroserviceHttpResponse();
        serverConnection.serve(httpRequest, httpResponse);
        return httpResponse.build();
    }

    @POST
    @Path(".*")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
    public Response post(@Context Request request, @Context FormParamIterator formParamIterator) {
        MicroserviceHttpRequest httpRequest = new MicroserviceHttpRequest(request, formParamIterator);
        MicroserviceHttpResponse httpResponse = new MicroserviceHttpResponse();
        serverConnection.serve(httpRequest, httpResponse);
        return httpResponse.build();
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }
}
