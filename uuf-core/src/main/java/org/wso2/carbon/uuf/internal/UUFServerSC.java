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

package org.wso2.carbon.uuf.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.uuf.api.Connector;
import org.wso2.carbon.uuf.internal.core.create.AppCreator;
import org.wso2.carbon.uuf.internal.core.create.AppDiscoverer;
import org.wso2.carbon.uuf.internal.io.ArtifactAppDiscoverer;
import org.wso2.carbon.uuf.internal.io.BundleClassLoaderProvider;
import org.wso2.carbon.uuf.internal.io.StaticResolver;
import org.wso2.carbon.uuf.spi.RenderableCreator;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OSGi service component for UUFServer.
 */
@Component(
        name = "org.wso2.carbon.uuf.internal.UUFServerSC",
        immediate = true,
        service = RequiredCapabilityListener.class,
        property = {
                "capability-name=org.wso2.carbon.uuf.spi.RenderableCreator",
                "component-key=wso2-uuf-server"
        }
)
public class UUFServerSC {
    private static final Set<RenderableCreator> RENDERABLE_CREATORS = new HashSet<>();
    private final AtomicInteger count = new AtomicInteger(0);
    private static final Logger log = LoggerFactory.getLogger(UUFServerSC.class);
    private UUFRegistry registry;

    public UUFServerSC() {
        this(createRegistry());
    }

    public UUFServerSC(UUFRegistry registry) {
        this.registry = registry;
    }

    private static UUFRegistry createRegistry() {
        AppDiscoverer appDiscoverer = new ArtifactAppDiscoverer();
        AppCreator appCreator = new AppCreator(RENDERABLE_CREATORS, new BundleClassLoaderProvider());
        StaticResolver staticResolver = new StaticResolver();
        return new UUFRegistry(appDiscoverer, appCreator, staticResolver);
    }

    /**
     * This bind method is invoked by OSGi framework whenever a new RenderableCreator is registered.
     *
     * @param renderableCreator registered renderable creator
     */
    @Reference(name = "renderablecreater",
               service = RenderableCreator.class,
               cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetRenderableCreator")
    @SuppressWarnings("unused")
    protected void setRenderableCreator(RenderableCreator renderableCreator) {
        if (!RENDERABLE_CREATORS.add(renderableCreator)) {
            throw new IllegalArgumentException(
                    "A RenderableCreator for '" + renderableCreator.getSupportedFileExtensions() +
                            "' extensions is already registered");
        }
        this.registry = createRegistry();
        log.info("RenderableCreator registered: " + renderableCreator.getClass().getName() + " for " +
                         renderableCreator.getSupportedFileExtensions() + " extensions.");
    }

    /**
     * This bind method is invoked by OSGi framework whenever a RenderableCreator is left.
     *
     * @param renderableCreator unregistered renderable creator
     */
    @SuppressWarnings("unused")
    protected void unsetRenderableCreator(RenderableCreator renderableCreator) {
        RENDERABLE_CREATORS.remove(renderableCreator);
        this.registry = createRegistry();
        log.info("RenderableCreator unregistered: " + renderableCreator.getClass().getName() + " for " +
                         renderableCreator.getSupportedFileExtensions() + " extensions.");
    }

    /**
     * This bind method is invoked by OSGi framework whenever a new Connector is registered.
     *
     * @param connector registered connector
     */
    @Reference(name = "connector",
               service = Connector.class,
               cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetConnector")
    @SuppressWarnings("unused")
    protected void setConnector(Connector connector) {
        connector.setRequestServer((request, response) -> {
            MDC.put("uuf-request", String.valueOf(count.incrementAndGet()));
            registry.serve(request, response);
            MDC.remove("uuf-request");
            return response;
        });
        log.info("Connector registered: " + connector.getClass().getName());
    }

    /**
     * This bind method is invoked by OSGi framework whenever a Connector is left.
     *
     * @param connector unregistered connector
     */
    @SuppressWarnings("unused")
    protected void unsetConnector(Connector connector) {
        log.info("Connector unregistered: " + connector.getClass().getName());
    }
}
