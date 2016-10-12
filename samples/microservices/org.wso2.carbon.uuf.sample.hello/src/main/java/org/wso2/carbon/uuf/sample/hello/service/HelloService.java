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
@Path("/pets-store/apis")
public class HelloService implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(HelloService.class);

    @Activate
    protected void activate(BundleContext bundleContext) {
        log.debug("Pets-store hello service activated.");
    }

    @Deactivate
    protected void deactivate() {
        log.debug("Pets-store hello service deactivated.");
    }

    @GET
    @Path("/hello")
    public String hello(@Context Request request, @Context MultivaluedMap multivaluedMap) {
        log.info("Accessed Secured Hello Service");
        MicroserviceHttpRequest httpRequest = new MicroserviceHttpRequest(request, multivaluedMap);
        String userName = API.getSessionUserName(request, httpRequest.getContextPath());
        return "Hello " + userName + "!!";
    }

    @GET
    @Path("/public/hello")
    public String unsecuredHello() {
        log.info("Accessed Unsecured Hello Service");
        return "Hello there !!";
    }
}
