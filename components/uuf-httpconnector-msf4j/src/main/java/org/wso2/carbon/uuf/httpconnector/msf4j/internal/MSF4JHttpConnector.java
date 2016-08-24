package org.wso2.carbon.uuf.httpconnector.msf4j.internal;


import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.ServerConnection;
import org.wso2.carbon.uuf.httpconnector.msf4j.UUFMicroservice;
import org.wso2.carbon.uuf.spi.HttpConnector;
import org.wso2.msf4j.Microservice;

import java.util.Dictionary;
import java.util.Hashtable;

@Component(name = "MSF4JHttpConnector",
           service = {HttpConnector.class},
           immediate = true)
public class MSF4JHttpConnector implements HttpConnector {

    private static final Logger log = LoggerFactory.getLogger(UUFMicroservice.class);
    private ServerConnection serverConnection;
    private BundleContext bundleContext;

    @Activate
    protected void activate() {
        this.bundleContext = FrameworkUtil.getBundle(MSF4JHttpConnector.class).getBundleContext();
        log.debug("MSF4JHttpConnector activated.");
    }

    @Deactivate
    protected void deactivate() {
        this.bundleContext = null;
        log.debug("MSF4JHttpConnector deactivated.");
    }

    @Override
    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public void registerContextPath(String contextPath) {
        Dictionary<String, String> dictionary = new Hashtable<>();
        dictionary.put("contextPath", contextPath);
        bundleContext.registerService(Microservice.class, new UUFMicroservice(serverConnection), dictionary);
    }
}
