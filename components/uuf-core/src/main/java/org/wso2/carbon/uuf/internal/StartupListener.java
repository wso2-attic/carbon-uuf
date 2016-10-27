/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.uuf.internal.core.create.AppDeployer;
import org.wso2.carbon.uuf.spi.HttpConnector;

@Component(name = "org.wso2.carbon.uuf.internal.StartupListener",
           service = RequiredCapabilityListener.class,
           immediate = true,
           property = {
                   "componentName=wso2-uuf-startup-listener"
           }
)
public class StartupListener implements RequiredCapabilityListener {

    private static final Logger log = LoggerFactory.getLogger(StartupListener.class);

    private AppDeployer appDeployer;

    @Reference(name = "httpConnector",
               service = HttpConnector.class,
               cardinality = ReferenceCardinality.AT_LEAST_ONE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetHttpConnector")
    public void setHttpConnector(HttpConnector httpConnector) {
        log.debug("HttpConnector '" + httpConnector.getClass().getName() + "' registered.");
    }

    public void unsetHttpConnector(HttpConnector httpConnector) {
        log.debug("HttpConnector '" + httpConnector.getClass().getName() + "' unregistered.");
    }

    @Reference(name = "appDeployer",
               service = AppDeployer.class,
               cardinality = ReferenceCardinality.MANDATORY,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetAppDeployer")
    public void setAppDeployer(AppDeployer appDeployer) {
        this.appDeployer = appDeployer;
    }

    public void unsetAppDeployer(AppDeployer appDeployer) {
        this.appDeployer = null;
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        log.debug("StartupListener activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        appDeployer = null;
        log.debug("StartupListener deactivated.");
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        appDeployer.deploy();
    }
}
