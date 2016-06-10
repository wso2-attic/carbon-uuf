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

package org.wso2.uuf.connector.msf4j;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.Connector;
import org.wso2.carbon.uuf.api.RequestServer;
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

/**
 * UUF Connector for MSF4J.
 */
@Component(name = "org.wso2.carbon.uuf.connector.Microservice",
           service = Microservice.class,
           immediate = true)
@Path("/")
public class UUFMicroservice implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(UUFMicroservice.class);

    @GET
    @Path(".*")
    public Response get(@Context HttpRequest request) {
        return execute(request, null);
    }

    @POST
    @Path(".*")
    @Produces({"text/plain"})
    public void post(@Context HttpStreamer httpStreamer, @Context HttpRequest nettyRequest) {
        httpStreamer.callback(new HttpStreamHandlerImpl(this, nettyRequest));
    }

    private Response execute(HttpRequest nettyRequest, byte[] contentBytes) {
        MicroserviceHttpRequest httpRequest = new MicroserviceHttpRequest(nettyRequest, contentBytes);
        MicroserviceHttpResponse httpResponse = new MicroserviceHttpResponse();
        requestServer.serve(httpRequest, httpResponse);
        return httpResponse.build();
    }


    private static class HttpStreamHandlerImpl implements HttpStreamHandler {
        private final ByteArrayOutputStream content = new ByteArrayOutputStream();
        private final UUFMicroservice UUFMicroservice;
        private final HttpRequest nettyRequest;

        public HttpStreamHandlerImpl(UUFMicroservice UUFMicroservice,
                                     HttpRequest nettyRequest) {
            this.UUFMicroservice = UUFMicroservice;
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

            Response response = UUFMicroservice.execute(nettyRequest, content.toByteArray());
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
