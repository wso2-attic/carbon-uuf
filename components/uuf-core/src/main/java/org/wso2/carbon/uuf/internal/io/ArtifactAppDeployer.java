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

package org.wso2.carbon.uuf.internal.io;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.exception.FileOperationException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.UUFServer;
import org.wso2.carbon.uuf.internal.deployment.AppCreator;
import org.wso2.carbon.uuf.internal.deployment.AppDeployer;
import org.wso2.carbon.uuf.internal.deployment.ClassLoaderProvider;
import org.wso2.carbon.uuf.internal.io.util.ZipArtifactHandler;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.spi.RenderableCreator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * UUF app deployer.
 */
public class ArtifactAppDeployer implements AppDeployer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactAppDeployer.class);

    private final Path appsRepository;
    private final AppCreator appCreator;
    private final ConcurrentMap<String, App> deployedApps;
    private final ConcurrentMap<String, AppArtifact> pendingToDeployArtifacts;
    private final Object lock;

    public ArtifactAppDeployer(Set<RenderableCreator> renderableCreators) {
        this(Paths.get(System.getProperty("carbon.home", "."), "deployment", "uufapps").toString(), renderableCreators);
    }

    public ArtifactAppDeployer(String appsRepositoryPath, Set<RenderableCreator> renderableCreators) {
        this(appsRepositoryPath, renderableCreators, new BundleClassLoaderProvider());
    }

    public ArtifactAppDeployer(String appsRepositoryPath, Set<RenderableCreator> renderableCreators,
                               ClassLoaderProvider classLoaderProvider) {
        this(Paths.get(appsRepositoryPath), new AppCreator(renderableCreators, classLoaderProvider));
    }

    public ArtifactAppDeployer(Path appsRepository, AppCreator appCreator) {
        this.appsRepository = appsRepository.toAbsolutePath();
        this.appCreator = appCreator;
        this.deployedApps = new ConcurrentHashMap<>();
        this.pendingToDeployArtifacts = new ConcurrentHashMap<>();
        this.lock = new Object();
    }

    @Override
    public Set<String> deploy() {
        Stream<Path> contents;
        try {
            contents = Files.list(appsRepository);
        } catch (IOException e) {
            throw new FileOperationException("Cannot list UUF apps in '" + appsRepository + "' directory.", e);
        }

        contents.filter(Files::isDirectory)
                .forEach(appPath -> {
                    Pair<String, String> appNameContextPath = getAppNameContextPath(appPath);
                    pendingToDeployArtifacts.put(appNameContextPath.getRight(),
                                                 new AppArtifact(appNameContextPath.getLeft(), appPath));
                    LOGGER.debug("UUF app '{}' added to the pending deployments list.", appNameContextPath.getLeft());
                });
        return Collections.unmodifiableSet(pendingToDeployArtifacts.keySet());
    }

    @Override
    public App getApp(String contextPath) {
        App app = deployedApps.get(contextPath);
        if (app != null) {
            return app;
        } else {
            if (pendingToDeployArtifacts.containsKey(contextPath)) {
                return deployApp(contextPath);
            } else {
                return null;
            }
        }
    }

    private Pair<String, String> getAppNameContextPath(Path appPath) {
        // Fully qualified name of the app is equals to the name of the app directory. This is guaranteed by the UUF
        // Maven plugin.
        String appFullyQualifiedName;
        if (ZipArtifactHandler.isZipArtifact(appPath)) {
            appFullyQualifiedName = ZipArtifactHandler.getAppName(appPath);
        } else {
            appFullyQualifiedName = appPath.getFileName().toString();
        }
        // TODO: 6/28/16 deployment.properties can override app's context path
        return Pair.of(appFullyQualifiedName, ("/" + NameUtils.getSimpleName(appFullyQualifiedName)));
    }

    private App deployApp(String contextPath) {
        App createdApp;
        synchronized (lock) {
            AppArtifact appArtifact = pendingToDeployArtifacts.remove(contextPath);
            if (appArtifact == null) {
                // App is deployed before acquiring the lock.
                return deployedApps.get(contextPath);
            }
            if (!Files.exists(appArtifact.appPath)) {
                // Somehow artifact has been removed/deleted. So we cannot create an app from it.
                LOGGER.warn("Cannot deploy UUF app in '{}' as it does not exists anymore.", appArtifact.appPath);
                return null;
            }
            try {
                createdApp = createApp(appArtifact.appName, contextPath, appArtifact.appPath);
            } catch (Exception e) {
                // catching any/all exception/s
                if (UUFServer.isDevModeEnabled()) {
                    /* If the server is in the developer mode, add the artifact back to the 'pendingToDeployArtifacts'
                    map so the developer can correct the error and attempt to re-deploy the artifact. */
                    pendingToDeployArtifacts.put(contextPath, appArtifact);
                }
                throw new UUFException("An error occurred while deploying UUF app in '" + appArtifact.appPath + "'.",
                                       e);
            }
            deployedApps.put(createdApp.getContextPath(), createdApp);
        }
        LOGGER.info("UUF app '{}' deployed for context path '{}'.", createdApp.getName(), createdApp.getContextPath());
        return createdApp;
    }

    private App createApp(String appName, String appContextPath, Path appPath) {
        ArtifactAppReference appReference;
        if (ZipArtifactHandler.isZipArtifact(appPath)) {
            appReference = new ArtifactAppReference(ZipArtifactHandler.unzip(appName, appPath));
        } else {
            appReference = new ArtifactAppReference(appPath);
        }
        return appCreator.createApp(appReference, appContextPath);
    }

    private static class AppArtifact {

        private final String appName;
        private final Path appPath;

        public AppArtifact(String appName, Path appPath) {
            this.appName = appName;
            this.appPath = appPath;
        }
    }
}
