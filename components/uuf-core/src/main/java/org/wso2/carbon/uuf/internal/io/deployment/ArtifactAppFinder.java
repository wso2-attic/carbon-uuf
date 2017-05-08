/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.internal.io.deployment;

import org.apache.commons.lang3.tuple.Pair;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.reference.AppReference;
import org.wso2.carbon.uuf.exception.FileOperationException;
import org.wso2.carbon.uuf.internal.deployment.AppFinder;
import org.wso2.carbon.uuf.internal.exception.DeploymentException;
import org.wso2.carbon.uuf.internal.io.reference.ArtifactAppReference;
import org.wso2.carbon.uuf.internal.io.util.ZipArtifactHandler;
import org.wso2.carbon.uuf.internal.util.NameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An app finder that locates apps from a directory.
 *
 * @since 1.0.0
 */
@Component(name = "org.wso2.carbon.uuf.internal.io.deployment.ArtifactAppFinder",
           service = AppFinder.class,
           immediate = true,
           property = {
                   "componentName=wso2-uuf-app-finder"
           }
)
public class ArtifactAppFinder implements AppFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactAppFinder.class);

    private final Path appsRepository;
    private final Map<String, AppDetails> availableApps;

    /**
     * Create a new app finder that locates apps from {@code <CARBON_HOME>/deployment/uufapps} directory.
     */
    public ArtifactAppFinder() {
        this(Paths.get(System.getProperty("carbon.home", "."), "deployment", "uufapps"));
    }

    /**
     * Create a new app finder that locates apps from the given directory.
     *
     * @param appsRepository app repository directory
     */
    public ArtifactAppFinder(Path appsRepository) {
        this.appsRepository = appsRepository;
        this.availableApps = new HashMap<>();
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        LOGGER.debug("ArtifactAppFinder activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        LOGGER.debug("ArtifactAppFinder deactivated.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Pair<String, String>> getAvailableApps() {
        List<AppDetails> foundApps = findApps(appsRepository);
        if (foundApps.isEmpty()) {
            throw new DeploymentException("No apps were found in '" + appsRepository + "'.");
        }
        List<Pair<String, String>> appNameContextPath = new ArrayList<>();
        for (AppDetails appDetails : foundApps) {
            availableApps.put(appDetails.getAppContextPath(), appDetails);
            appNameContextPath.add(Pair.of(appDetails.getAppName(), appDetails.getAppContextPath()));
            LOGGER.debug("UUF app '{}' found at '{}' for context path '{}'.", appDetails.getAppName(),
                         appDetails.getAppReference().getPath(), appDetails.getAppContextPath());
        }
        return appNameContextPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AppReference> getAppReference(String appContextPath) {
        return Optional.ofNullable(availableApps.get(appContextPath).getAppReference());
    }

    private List<AppDetails> findApps(Path appsRepository) {
        try {
            return Files.list(appsRepository)
                    .filter(Files::isDirectory)
                    .map(this::getAppDetails)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new FileOperationException("Cannot list UUF apps in '" + appsRepository + "' directory.", e);
        }
    }

    private AppDetails getAppDetails(Path appPath) {
        /* Fully qualified name of the app is equals to the name of the app directory. This is guaranteed by the UUF
        Maven plugin. */
        String appFullyQualifiedName;
        if (ZipArtifactHandler.isZipArtifact(appPath)) {
            appFullyQualifiedName = ZipArtifactHandler.getAppName(appPath);
        } else {
            appFullyQualifiedName = appPath.getFileName().toString();
        }
        // TODO: 5/4/17 through deployment.yaml Dev-Ops should be able to override app's context path
        String appContextPath = "/" + NameUtils.getSimpleName(appFullyQualifiedName);
        AppReference appReference = new ArtifactAppReference(appPath);

        return new AppDetails(appFullyQualifiedName, appContextPath, appReference);
    }

    /**
     * A data holder that holds name, context path, and the app reference of an app.
     *
     * @since 1.0.0
     */
    private static class AppDetails {

        private final String appName;
        private final String appContextPath;
        private final AppReference appReference;

        AppDetails(String appName, String appContextPath, AppReference appReference) {
            this.appName = appName;
            this.appContextPath = appContextPath;
            this.appReference = appReference;
        }

        String getAppName() {
            return appName;
        }

        String getAppContextPath() {
            return appContextPath;
        }

        AppReference getAppReference() {
            return appReference;
        }
    }
}
