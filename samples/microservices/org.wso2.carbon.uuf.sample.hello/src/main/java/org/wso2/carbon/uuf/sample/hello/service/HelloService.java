package org.wso2.carbon.uuf.sample.hello.service;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.internal.core.auth.SessionRegistry;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.delegates.client.MSF4JClientRequestContext;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;

@Component(name = "HelloService",
           service = {Microservice.class},
           immediate = true)
@SuppressWarnings("unused")
@Path("/pets-store/hello")
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
    public String hello(@Context Request request)  {
        System.out.println("Hello");
        String userName = API.getSessionData(request);
        BundleContext bundleContext1 = this.bundleContext;
        return "Hello " + userName;
    }
}