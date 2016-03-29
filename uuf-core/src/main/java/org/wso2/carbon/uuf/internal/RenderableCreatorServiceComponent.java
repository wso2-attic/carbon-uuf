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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.uuf.core.create.RenderableCreator;

import java.util.HashSet;
import java.util.Set;

@Component(
        name = "org.wso2.carbon.uuf.internal.RenderableCreatorServiceComponent",
        immediate = true,
        service = RequiredCapabilityListener.class,
        property = { "capability-name=org.wso2.carbon.uuf.core.create.RenderableCreator",
                "component-key=wso2-uuf-server" })
public class RenderableCreatorServiceComponent implements RequiredCapabilityListener {

    private final static Logger log = LoggerFactory.getLogger(RenderableCreatorServiceComponent.class);
    RenderableCreatorsRepository creatorsRepository = RenderableCreatorsRepository.getInstance();

    @Activate
    protected void start(final BundleContext bundleContext) {
    }

    @Reference(
            name = "renderablecreater",
            service = RenderableCreator.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRenderableCreator")
    protected void setRenderableCreator(RenderableCreator renderableCreator) {
        this.creatorsRepository.add(renderableCreator);
    }

    protected void unsetRenderableCreator(RenderableCreator renderableCreator) {
        this.creatorsRepository.remove(renderableCreator);
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        Bundle currentBundle = FrameworkUtil.getBundle(RenderableCreatorServiceComponent.class);
        BundleContext bundleContext = currentBundle.getBundleContext();
        bundleContext.registerService(RenderableCreatorServiceComponent.class, this, null);
        log.info("All renderable creaters are available");
    }
}
