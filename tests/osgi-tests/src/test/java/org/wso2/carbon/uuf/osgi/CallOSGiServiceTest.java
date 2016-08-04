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
public class CallOSGiServiceTest {

    /**
     * Adding all the required OSGi bundles bundles into pax exam container
     *
     * @return Options list of pax exam container
     */
    @Configuration
    public Option[] createConfiguration() {
        OSGiTestUtils.setEnv();
        Option[] options = CoreOptions.options(mavenBundle().artifactId("org.wso2.carbon.deployment.engine").
                                                       groupId("org.wso2.carbon.deployment").versionAsInProject(),
                                               mavenBundle().artifactId("org.wso2.carbon.deployment.notifier").
                                                       groupId("org.wso2.carbon.deployment").version("5.1.0-SNAPSHOT"),
                                               mavenBundle().artifactId("geronimo-jms_1.1_spec").
                                                       groupId("org.apache.geronimo.specs").version("1.1.1"),
                                               mavenBundle().artifactId("commons-pool").
                                                       groupId("commons-pool.wso2").version("1.5.6.wso2v1"),
                                               mavenBundle().artifactId("org.wso2.carbon.uuf.sample.pets-store.bundle").
                                                       groupId("org.wso2.carbon.uuf.sample").version("1.0.0-SNAPSHOT"),
                                               mavenBundle().artifactId("commons-io").groupId("commons-io.wso2")
                                                       .version("2.4.0.wso2v1"),
                                               mavenBundle().artifactId("org.wso2.carbon.jndi")
                                                       .groupId("org.wso2.carbon.jndi").version("1.0.0"),
                                               mavenBundle().artifactId("org.wso2.carbon.caching")
                                                       .groupId("org.wso2.carbon.caching")
                                                       .version("1.0.0"),
                                               mavenBundle().artifactId("gson").
                                                       groupId("com.google.code.gson").version("2.6.2"),
                                               mavenBundle().artifactId("guava").
                                                       groupId("com.google.guava").version("18.0"),
                                               mavenBundle().artifactId("commons-lang3").
                                                       groupId("org.apache.commons").version("3.1"),
                                               mavenBundle().artifactId("asm").
                                                       groupId("org.ow2.asm").version("5.1"),
                                               mavenBundle().artifactId("org.wso2.carbon.uuf.renderablecreator.html").
                                                       groupId("org.wso2.carbon.uuf").version("1.0.0-SNAPSHOT"),
                                               mavenBundle().artifactId("org.wso2.carbon.uuf.core").
                                                       groupId("org.wso2.carbon.uuf").version("1.0.0-SNAPSHOT")
        );
        return OSGiTestUtils.getDefaultPaxOptions(options);
    }

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Test
    public void testDeploymentService() throws CarbonDeploymentException {
        ServiceRegistration serviceRegistration = bundleContext.registerService(PetsManager.class, name -> null, null);
        ServiceReference serviceReference = bundleContext.getServiceReference(PetsManager.class.getName());

        //Check for 'Pets Store' service reference
        Assert.assertNotNull(serviceReference, "Pets Store Service Reference is null");
        PetsManager petsManager = (PetsManager) bundleContext.getService(serviceReference);

        //Check for the availability of 'Pets Store' service
        Assert.assertNotNull(petsManager, "Pets Store Service is null");

        //Directly call 'Pets Store' OSGi service
        Assert.assertEquals(petsManager.getHelloMessage("Alice"), "Hello Alice!",
                            "Pets Store Service, getHelloMessage is not working properly");

        //Call 'Pets Store' service through UUF API
        Assert.assertEquals(API.callOSGiService("org.wso2.carbon.uuf.sample.petsstore.bundle.service.PetsManager",
                                                "getHelloMessage", "Bob"), "Hello Bob!");
        serviceRegistration.unregister();
    }

}
