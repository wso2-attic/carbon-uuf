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

package org.wso2.carbon.uuf.osgi;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.osgi.utils.OSGiTestUtils;
import org.wso2.carbon.uuf.sample.petsstore.bundle.service.PetsStoreService;

import javax.inject.Inject;
import java.util.Map;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;


@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiServicesTest {

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    /**
     * Adding all the required OSGi bundles bundles into pax exam container.
     *
     * @return Options list of pax exam container
     * @throws Exception
     */
    @Configuration
    public Option[] createConfiguration() throws Exception {
        OSGiTestUtils.setEnv();
        Option[] options = CoreOptions.options(
                // Carbon
                getBundleOption("org.wso2.carbon.jndi", "org.wso2.carbon.jndi"),
                getBundleOption("org.wso2.carbon.caching", "org.wso2.carbon.caching"),
                getBundleOption("org.wso2.carbon.deployment.engine", "org.wso2.carbon.deployment"),
                getBundleOption("org.wso2.carbon.deployment.notifier", "org.wso2.carbon.deployment"),
                // Others
                getBundleOption("geronimo-jms_1.1_spec", "org.apache.geronimo.specs"),
                getBundleOption("commons-pool", "commons-pool.wso2"),
                getBundleOption("commons-io", "commons-io.wso2"),
                getBundleOption("gson", "com.google.code.gson"),
                getBundleOption("guava", "com.google.guava"),
                getBundleOption("commons-lang3", "org.apache.commons"),
                getBundleOption("asm", "org.ow2.asm"),
                // UUF
                getBundleOption("org.wso2.carbon.uuf.core", "org.wso2.carbon.uuf"),
                getBundleOption("org.wso2.carbon.uuf.renderablecreator.html", "org.wso2.carbon.uuf"),
                getBundleOption("org.wso2.carbon.uuf.tests.dummy-http-connector", "org.wso2.carbon.uuf.tests"),
                getBundleOption("org.wso2.carbon.uuf.sample.pets-store.bundle", "org.wso2.carbon.uuf.sample"),
                // MSF4J
                getBundleOption("msf4j-core", "org.wso2.msf4j"),
                getBundleOption("org.apache.servicemix.bundles.commons-beanutils", "org.apache.servicemix.bundles"),
                getBundleOption("javax.ws.rs-api", "javax.ws.rs"),
                getBundleOption("netty-buffer", "io.netty"),
                getBundleOption("netty-common", "io.netty"),
                getBundleOption("netty-handler", "io.netty"),
                getBundleOption("netty-transport", "io.netty"),
                getBundleOption("netty-codec", "io.netty"),
                getBundleOption("netty-codec-http", "io.netty"),
                getBundleOption("disruptor", "org.wso2.lmax"),
                getBundleOption("org.wso2.carbon.transport.http.netty", "org.wso2.carbon.transport"),
                getBundleOption("org.wso2.carbon.messaging", "org.wso2.carbon.messaging")
        );
        return OSGiTestUtils.getDefaultPaxOptions(options);
    }

    @Test
    public void testPetsStoreService() {
        ServiceReference serviceReference = bundleContext.getServiceReference(PetsStoreService.class.getName());
        Assert.assertNotNull(serviceReference, "Pets Store Service Reference is null.");

        PetsStoreService petsStoreService = (PetsStoreService) bundleContext.getService(serviceReference);
        Assert.assertNotNull(petsStoreService, "Pets Store Service is null.");

        String serviceOutput = petsStoreService.getHelloMessage("Alice");
        Assert.assertEquals(serviceOutput, "Hello Alice!",
                            "Pets Store Service, getHelloMessage is not working properly.");
    }

    @Test
    public void testOSGiServicesAPI() {
        String outputForCallOSGiService = API.callOSGiService(
                "org.wso2.carbon.uuf.sample.petsstore.bundle.service.PetsStoreService",
                "getHelloMessage", "Bob").toString();
        Assert.assertEquals(outputForCallOSGiService, "Hello Bob!");

        Map<String, Object> osgiServices = API.getOSGiServices(
                "org.wso2.carbon.uuf.sample.petsstore.bundle.service.PetsStoreService");
        Object petsStoreService = osgiServices.get(
                "org.wso2.carbon.uuf.sample.petsstore.bundle.internal.impl.PetsManagerImpl");
        Assert.assertNotNull(petsStoreService,
                             "PetsManagerImpl service wasn't retrieved from getOSGiServices method.");

        String serviceOutput = ((PetsStoreService) petsStoreService).getHelloMessage("Alice");
        Assert.assertEquals(serviceOutput, "Hello Alice!");
    }

    /**
     * Returns the maven bundle option for pax-exam container.
     *
     * @param artifactId Bundle artifact id
     * @param groupId    Bundle group id
     * @return Maven bundle option
     */
    private Option getBundleOption(String artifactId, String groupId) {
        return mavenBundle().artifactId(artifactId).groupId(groupId).versionAsInProject();
    }
}
