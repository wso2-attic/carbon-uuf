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

import org.testng.annotations.Listeners;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.osgi.utils.OSGiTestUtils;
import org.wso2.carbon.uuf.sample.petsstore.bundle.service.PetsManager;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;


@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiServicesTest {

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    /**
     * Adding all the required OSGi bundles bundles into pax exam container
     *
     * @return Options list of pax exam container
     */
    @Configuration
    public Option[] createConfiguration() {
        OSGiTestUtils.setEnv();
        Option[] options = CoreOptions.options(
                getBundleOption("org.wso2.carbon.deployment.engine", "org.wso2.carbon.deployment"),
                getBundleOption("org.wso2.carbon.deployment.notifier", "org.wso2.carbon.deployment", "5.1.0-SNAPSHOT"),
                getBundleOption("geronimo-jms_1.1_spec", "org.apache.geronimo.specs", "1.1.1"),
                getBundleOption("commons-pool", "commons-pool.wso2", "1.5.6.wso2v1"),
                getBundleOption("org.wso2.carbon.uuf.sample.pets-store.bundle", "org.wso2.carbon.uuf.sample",
                                "1.0.0-SNAPSHOT"),
                getBundleOption("commons-io", "commons-io.wso2", "2.4.0.wso2v1"),
                getBundleOption("org.wso2.carbon.jndi", "org.wso2.carbon.jndi", "1.0.0"),
                getBundleOption("org.wso2.carbon.caching", "org.wso2.carbon.caching", "1.0.0"),
                getBundleOption("gson", "com.google.code.gson", "2.6.2"),
                getBundleOption("guava", "com.google.guava", "18.0"),
                getBundleOption("commons-lang3", "org.apache.commons", "3.1"),
                getBundleOption("asm", "org.ow2.asm", "5.1"),
                getBundleOption("org.wso2.carbon.uuf.renderablecreator.html", "org.wso2.carbon.uuf", "1.0.0-SNAPSHOT"),
                getBundleOption("org.wso2.carbon.uuf.core", "org.wso2.carbon.uuf", "1.0.0-SNAPSHOT")
        );
        return OSGiTestUtils.getDefaultPaxOptions(options);
    }

    @Test
    public void testDeploymentService() throws CarbonDeploymentException {
        ServiceRegistration serviceRegistration = bundleContext.registerService(PetsManager.class, name -> null, null);

        //Check for 'Pets Store' service reference
        ServiceReference serviceReference = bundleContext.getServiceReference(PetsManager.class.getName());
        Assert.assertNotNull(serviceReference, "Pets Store Service Reference is null");

        //Check for the availability of 'Pets Store' service
        PetsManager petsManager = (PetsManager) bundleContext.getService(serviceReference);
        Assert.assertNotNull(petsManager, "Pets Store Service is null");

        //Directly call 'Pets Store' OSGi service
        String serviceOutput = petsManager.getHelloMessage("Alice");
        Assert.assertEquals(serviceOutput, "Hello Alice!",
                            "Pets Store Service, getHelloMessage is not working properly");

        //Call 'Pets Store' service through UUF API
        String APIOutput = API.callOSGiService("org.wso2.carbon.uuf.sample.petsstore.bundle.service.PetsManager",
                                               "getHelloMessage", "Bob").toString();
        Assert.assertEquals(APIOutput, "Hello Bob!");
        serviceRegistration.unregister();
    }

    private Option getBundleOption(String artifactId, String groupId, String version) {
        return mavenBundle().artifactId(artifactId).groupId(groupId).version(version);
    }

    private Option getBundleOption(String artifactId, String groupId) {
        return mavenBundle().artifactId(artifactId).groupId(groupId).versionAsInProject();
    }
}
