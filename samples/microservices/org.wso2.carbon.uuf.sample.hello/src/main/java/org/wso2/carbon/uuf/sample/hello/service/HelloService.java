package org.wso2.carbon.uuf.sample.hello.service;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.httpconnector.msf4j.MicroserviceHttpRequest;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;

@Component(name = "HelloService",
           service = {Microservice.class},
           immediate = true)
@SuppressWarnings("unused")
@Path("/pets-store/api/hello")
public class HelloService implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(HelloService.class);
    private BundleContext bundleContext;

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        log.debug("MicroserviceDeployer activated.");
    }

    @Deactivate
    protected void deactivate() {
        this.bundleContext = null;
        log.debug("MicroserviceDeployer deactivated.");
    }

    @GET
    @Path("/say")
    public String hello(@Context Request request, @Context MultivaluedMap multivaluedMap) {
        System.out.println("Hello Service");
        MicroserviceHttpRequest httpRequest = new MicroserviceHttpRequest(request, multivaluedMap);
        String userName = API.getSessionUserName(request, httpRequest.getContextPath());
        return "Hello " + userName + "!!";
    }
}