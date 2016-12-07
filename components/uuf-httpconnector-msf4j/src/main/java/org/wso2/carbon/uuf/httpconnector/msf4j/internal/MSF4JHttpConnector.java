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

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.Server;
import org.wso2.carbon.uuf.httpconnector.msf4j.UUFMicroservice;
import org.wso2.carbon.uuf.spi.HttpConnector;
import org.wso2.msf4j.Microservice;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * This class generates connections between the each application's context path and MSF4j, to facilitate communication
 * between the browser and UUF core.
 */
@Component(name = "org.wso2.carbon.uuf.httpconnector.msf4j.internal.MSF4JHttpConnector",
           service = HttpConnector.class,
           immediate = true)
@SuppressWarnings("unused")
public class MSF4JHttpConnector implements HttpConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(MSF4JHttpConnector.class);

    private Server uufServer;
    private BundleContext bundleContext;

    @Reference(name = "uufServer",
               service = Server.class,
               cardinality = ReferenceCardinality.MANDATORY,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetServer")
    @Override
    public void setServer(Server server) {
        this.uufServer = server;
    }

    public void unsetServer(Server server) {
        this.uufServer = null;
    }

    /**
     * Get called when this osgi component get registered.
     *
     * @param bundleContext Context of the osgi component.
     */
    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        LOGGER.debug("{} activated.", getClass().getName());
    }

    /**
     * Get called when this osgi component get unregistered.
     */
    @Deactivate
    protected void deactivate() {
        this.bundleContext = null;
        LOGGER.debug("{} deactivated.", getClass().getName());
    }

    /**
     * Create and register a microservice for each application using application's context path.
     *
     * @param appContextPath app context path
     */
    @Override
    public void registerAppContextPath(String appContextPath) {
        Dictionary<String, String> dictionary = new Hashtable<>();
        dictionary.put("contextPath", appContextPath);
        bundleContext.registerService(Microservice.class, new UUFMicroservice(uufServer), dictionary);
    }
}
