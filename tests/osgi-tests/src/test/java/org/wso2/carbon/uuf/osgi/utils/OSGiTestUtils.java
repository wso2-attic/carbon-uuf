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

package org.wso2.carbon.uuf.osgi.utils;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.wso2.carbon.kernel.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.repositories;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.url;

/**
 * This class contains Utility methods to configure PAX-EXAM container.
 *
 * @since 5.0.0
 */
public class OSGiTestUtils {

    /**
     * Returns a merged array of user specified options and default options.
     *
     * @param options custom options.
     * @return a merged array.
     */
    public static Option[] getDefaultPaxOptions(Option[] options) {
        return Stream.concat(Arrays.stream(getDefaultPaxOptions()), Arrays.stream(options))
                .toArray(Option[]::new);
    }

    /**
     * Set the environment prior to tests.
     *
     * @throws IOException
     */
    public static void setEnv() throws Exception {
        setCarbonHome();
        setStartupTime();
        copyFiles();
    }

    /**
     * Returns an array of default PAX-EXAM options.
     *
     * @return array of Options
     */
    private static Option[] getDefaultPaxOptions() {
        return options(
                repositories("http://maven.wso2.org/nexus/content/groups/wso2-public"),
                systemProperty("carbon.home").value(System.getProperty("carbon.home")),
                systemProperty(Constants.START_TIME).value(System.getProperty(Constants.START_TIME)),
                getUrlProvisionOption("testng", "org.testng"),
                getUrlProvisionOption("org.eclipse.osgi.services", "org.wso2.eclipse.osgi"),
                getUrlProvisionOption("pax-logging-api", "org.ops4j.pax.logging"),
                getUrlProvisionOption("pax-logging-log4j2", "org.ops4j.pax.logging"),
                getUrlProvisionOption("org.eclipse.equinox.simpleconfigurator", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("org.apache.felix.gogo.command", "org.apache.felix"),
                getUrlProvisionOption("org.apache.felix.gogo.runtime", "org.apache.felix"),
                getUrlProvisionOption("org.apache.felix.gogo.shell", "org.apache.felix"),
                getUrlProvisionOption("org.eclipse.equinox.app", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("org.eclipse.equinox.common", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("org.eclipse.equinox.concurrent", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("org.eclipse.equinox.console", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("org.eclipse.equinox.ds", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("org.eclipse.equinox.frameworkadmin", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("org.eclipse.equinox.frameworkadmin.equinox", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("org.eclipse.equinox.launcher", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("org.eclipse.equinox.preferences", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("org.eclipse.equinox.registry", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("org.eclipse.equinox.simpleconfigurator.manipulator", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("org.eclipse.equinox.util", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("org.eclipse.equinox.cm", "org.wso2.eclipse.equinox"),
                getUrlProvisionOption("snakeyaml", "org.wso2.orbit.org.yaml"),
                getUrlProvisionOption("org.wso2.carbon.core", "org.wso2.carbon")
        );
    }

    /**
     * Set the carbon home.
     */
    private static void setCarbonHome() {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        Path carbonHome = Paths.get(currentDir, "target", "carbon-home");
        System.setProperty("carbon.home", carbonHome.toString());
    }

    /**
     * Set the startup time to calculate the server startup time.
     */
    private static void setStartupTime() {
        if (System.getProperty(Constants.START_TIME) == null) {
            System.setProperty(Constants.START_TIME, System.currentTimeMillis() + "");
        }
    }

    /**
     * Copying resource files to the carbon home location.
     *
     * @throws IOException
     */
    private static void copyFiles() throws Exception {
        //Replace the existing carbon.yml file with populated carbon.yml file.
        copy(Paths.get("src", "test", "resources", "conf", "carbon.yml"),
             Paths.get(System.getProperty("carbon.home"), "conf", "carbon.yml"));

        //Replace the existing log4j2.xml file with populated log4j2.xml file.
        copy(Paths.get("src", "test", "resources", "conf", "log4j2.xml"), Paths.get("conf", "log4j2.xml"));

        //Replace the existing launch.properties file with populated launch.properties file.
        copy(Paths.get("src", "test", "resources", "conf", "osgi", "launch.properties"),
             Paths.get("conf", "osgi", "launch.properties"));

        //Copy deployment.yaml file
        copy(Paths.get("src", "test", "resources", "conf", "deployment.yml"), Paths.get("conf", "deployment.yml"));

        //Replace the existing "README.txt file with populated "README.txt file.
        copy(Paths.get("src", "test", "resources", "deployment", "README.txt"), Paths.get("deployment", "README.txt"));
    }

    /**
     * Copy files.
     *
     * @param sourcePath      Path for source
     * @param destinationPath Path for destination
     * @throws IOException
     */
    private static void copy(Path sourcePath, Path destinationPath) throws Exception {
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }

        sourcePath = Paths.get(basedir).resolve(sourcePath);
        destinationPath = getCarbonHome().resolve(destinationPath);

        createOutputFolderStructure(destinationPath);
        try {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new Exception("Error occurred while copying '" + sourcePath + "' file to " + destinationPath + ".",
                                e);
        }
    }

    /**
     * Return carbon home path
     *
     * @return Path for carbon home
     */
    private static Path getCarbonHome() {
        return Paths.get(System.getProperty("carbon.home"));
    }

    /**
     * Create the directory structure.
     *
     * @param destinationPath Path to file copying destination
     * @throws IOException
     */
    private static void createOutputFolderStructure(Path destinationPath) throws Exception {
        Path parentPath = destinationPath.getParent();
        try {
            Files.createDirectories(parentPath);
        } catch (IOException e) {
            throw new Exception("Error occurred while creating the directory '" + parentPath + "'.", e);
        }
    }

    /**
     * Return Url provision option of a particular maven bundle.
     *
     * @param artifactId Bundle artifact id
     * @param groupId    Bundle group id
     * @return Url provision option
     */
    private static UrlProvisionOption getUrlProvisionOption(String artifactId, String groupId) {
        return url(mavenBundle().artifactId(artifactId).groupId(groupId).versionAsInProject().getURL());
    }
}
