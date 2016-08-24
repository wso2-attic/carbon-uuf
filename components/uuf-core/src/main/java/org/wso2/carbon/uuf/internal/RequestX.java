package org.wso2.carbon.uuf.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.wso2.carbon.uuf.spi.HttpConnector;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component(name = "org.wso2.carbon.uuf.internal.RequestX",
           immediate = true
)
public class RequestX {
    private static Set<HttpConnector> httpConnectors = new HashSet<>();
    private static final Logger log = LoggerFactory.getLogger(UUFServer.class);
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * This bind method is invoked by OSGi framework whenever a new Connector is registered.
     *
     * @param connector registered connector
     */
    @Reference(name = "httpConnector",
               service = HttpConnector.class,
               cardinality = ReferenceCardinality.AT_LEAST_ONE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetHttpConnector")
    public void setHttpConnector(HttpConnector connector) {
        connector.setServerConnection((request, response) -> {
            MDC.put("uuf-request", String.valueOf(count.incrementAndGet()));
            //serve(request, response);
            MDC.remove("uuf-request");
            return response;
        });
        httpConnectors.add(connector);
        log.info("HttpConnector '" + connector.getClass().getName() + "' registered.");
    }

    /**
     * This bind method is invoked by OSGi framework whenever a Connector is left.
     *
     * @param connector unregistered connector
     */
    public void unsetHttpConnector(HttpConnector connector) {
        connector.setServerConnection(null);
        log.info("HttpConnector '" + connector.getClass().getName() + "' unregistered.");
    }

    public void registerHttpConnectors(String contextPath) {
        //registering each http connector for the context path
        httpConnectors.forEach(httpConnector -> {
            httpConnector.registerContextPath(contextPath);
        });
    }
}
