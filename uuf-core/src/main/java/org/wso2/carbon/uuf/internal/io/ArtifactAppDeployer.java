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

package org.wso2.carbon.uuf.internal.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.exception.DeploymentException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.core.create.AppCreator;
import org.wso2.carbon.uuf.internal.core.create.ClassLoaderProvider;
import org.wso2.carbon.uuf.internal.debug.Debugger;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.spi.RenderableCreator;
import org.wso2.carbon.uuf.spi.UUFAppRegistry;
import org.apache.commons.io.FilenameUtils;
import org.wso2.carbon.kernel.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * UUF app deployer.
 */
@Component(
        name = "org.wso2.carbon.uuf.internal.io.ArtifactAppDeployer",
        immediate = true,
        service = RequiredCapabilityListener.class,
        property = {
                "componentName=wso2-uuf-deployer"
        }
)
@SuppressWarnings("unused")
public class ArtifactAppDeployer implements Deployer, UUFAppRegistry, RequiredCapabilityListener {

    private static final Logger log = LoggerFactory.getLogger(ArtifactAppDeployer.class);
    private static final String ZIP_FILE_EXTENSION = "zip";
    private static final String UUFAPPS_DIR = "uufapps";
    private static final Path DIR_TEMP_UUFAPPS;
    private static final Path DIR_UUFAPPS;
    private static final Path CARBON_HOME = Utils.getCarbonHome();

    private final ArtifactType artifactType;
    private final URL location;
    private final Map<String, AppData> deployedApps;
    private final Map<String, Artifact> pendingToDeployArtifacts;
    private final Object lock;
    private final Set<RenderableCreator> renderableCreators;
    private final ClassLoaderProvider classLoaderProvider;
    private AppCreator appCreator;
    private BundleContext bundleContext;

    static {
        DIR_TEMP_UUFAPPS = CARBON_HOME.resolve("tmp").resolve(UUFAPPS_DIR).toAbsolutePath();
        DIR_UUFAPPS = CARBON_HOME.resolve("deployment").resolve(UUFAPPS_DIR).toAbsolutePath();
    }

    public ArtifactAppDeployer() {
        this.artifactType = new ArtifactType<>("uufapp");
        try {
            this.location = new URL("file:" + UUFAPPS_DIR);
        } catch (MalformedURLException e) {
            throw new UUFException("Cannot create URL 'file:uufapps'.", e);
        }
        this.deployedApps = new ConcurrentHashMap<>();
        this.pendingToDeployArtifacts = new ConcurrentHashMap<>();
        this.lock = new Object();
        this.renderableCreators = ConcurrentHashMap.newKeySet();
        this.classLoaderProvider = new BundleClassLoaderProvider();
    }

    @Override
    public void init() {
        log.debug("ArtifactAppDeployer initialized.");
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }

    @Override
    public URL getLocation() {
        return location;
    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        Pair<String, String> appNameContextPath;
        try {
            appNameContextPath = getAppNameContextPath(artifact);
        } catch (DeploymentException e) {
            throw new CarbonDeploymentException("Cannot deploy UUF app artifact in " + artifact.getPath() + ".", e);
        }
        if (deployedApps.containsKey(appNameContextPath.getRight())) {
            throw new CarbonDeploymentException(
                    "Cannot deploy UUF app artifact in '" + artifact.getPath() + "' for context path '" +
                            appNameContextPath.getRight() +
                            "' as another app is already registered for the same context path.");
        }

        pendingToDeployArtifacts.put(appNameContextPath.getRight(), artifact);
        log.debug("UUF app '" + appNameContextPath.getLeft() + "' added to the pending deployments list.");
        return appNameContextPath.getLeft();
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        Pair<String, String> appNameContextPath = getAppNameContextPath(artifact);
        if (deployedApps.containsKey(appNameContextPath.getRight())) {
            // This artifact is already deployed.
            AppData appData = getAppData(artifact);
            App createdApp = appData.getApp();
            deployedApps.put(createdApp.getContextPath(), appData);
            log.info("UUF app '" + createdApp.getName() + "' re-deployed for context path '" +
                             createdApp.getContextPath() + "'.");
            return createdApp.getName();
        } else {
            // This artifact is not deployed yet. It is in the pending list 'pendingToDeployArtifacts'. So new
            // changes will be picked up when it is actually deployed.
            return appNameContextPath.getLeft();
        }
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        String appName = (String) key;
        Optional<App> removedApp = deployedApps.values().stream()
                .filter(appData -> appData.getApp().getName().equals(appName))
                .findFirst()
                .map(removingAppData -> {
                    deployedApps.remove(removingAppData.getApp().getContextPath());
                    return removingAppData.getApp();
                });
        if (removedApp.isPresent()) {
            // App with 'appName' is deployed.
            log.info("UUF app '" + removedApp.get().getName() + "' undeployed for context '" +
                             removedApp.get().getContextPath() + "'.");
        } else {
            // App with 'appName' is not deployed yet.
            for (Map.Entry<String, Artifact> entry : pendingToDeployArtifacts.entrySet()) {
                Pair<String, String> appNameContextPath = getAppNameContextPath(entry.getValue());
                if (appName.equals(appNameContextPath.getLeft())) {
                    Artifact removedArtifact = pendingToDeployArtifacts.remove(appNameContextPath.getRight());
                    log.info("UUF app in '" + removedArtifact.getPath() + "' removed even before it deployed.");
                    break;
                }
            }
        }
    }

    private Pair<String, String> getAppNameContextPath(Artifact artifact) {
        // TODO: 6/28/16 deployment.properties can override app's context path
        // Fully qualified name of the app is equals to the name od the app directory. This is guaranteed by the UUF
        // Maven plugin.
        String appFullyQualifiedName;
        String extension = FilenameUtils.getExtension(artifact.getPath());
        if (extension.equals(ZIP_FILE_EXTENSION)) {
            appFullyQualifiedName = getZipFileName(artifact.getFile());
        } else {
            appFullyQualifiedName = Paths.get(artifact.getPath()).getFileName().toString();
        }
        return Pair.of(appFullyQualifiedName, ("/" + NameUtils.getSimpleName(appFullyQualifiedName)));
    }

    private App deployApp(String contextPath) {
        App app;
        AppData appData;
        synchronized (lock) {
            Artifact artifact = pendingToDeployArtifacts.remove(contextPath);
            if (artifact == null) {
                // App is deployed before acquiring the lock.
                return deployedApps.get(contextPath).getApp();
            }
            if (!artifact.getFile().exists()) {
                // Somehow artifact has been removed/deleted. So we cannot create an app from it.
                log.warn("Cannot deploy UUF app in '" + artifact.getPath() + "' as it does not exists anymore.");
                return null;
            }
            try {
                appData = getAppData(artifact);
                app = appData.getApp();
            } catch (Exception e) {
                // catching any/all exception/s
                if (Debugger.isDebuggingEnabled()) {
                    // If the server is in the debug mode, add the artifact back to the 'pendingToDeployArtifacts'
                    // map so the Dev can correct the error and attempt to re-deploy the artifact.
                    pendingToDeployArtifacts.put(contextPath, artifact);
                }
                throw new UUFException("An error occurred while deploying UUF app in '" + artifact.getPath() + "'.", e);
            }
            String appContextPath = app.getContextPath();
            deployedApps.put(appContextPath, appData);
        }
        log.info("UUF app '" + app.getName() + "' deployed for context path '" + app.getContextPath() + "'.");
        return app;
    }

    /**
     * Unzip file
     *
     * @param file File to be unzipped
     * @return Unzipped location
     */
    private Path unzip(File file) {
        ZipFile zipFile;
        int entryCount = 0;
        String entryName;
        String firstEntryName = null;
        File unzipFolder = Paths.get(String.valueOf(DIR_TEMP_UUFAPPS)).toFile();

        if (!Files.exists(DIR_TEMP_UUFAPPS)) {
            if (!unzipFolder.mkdir()) {
                new DeploymentException("Error occurred while creating the folder " +
                                                DIR_TEMP_UUFAPPS.relativize(CARBON_HOME) + ".");
            }
        }
        try {
            zipFile = new ZipFile(file);
        } catch (IOException e) {
            throw new DeploymentException("Error encountered while opening the zip file, " + file.getName() + ".", e);
        }

        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();
            entryName = zipEntry.getName();
            // If a folder already exists in the tmp folder with the same app name, delete the folder before unzipping
            // the new app.
            if (++entryCount == 1) {
                Path appDirectory = DIR_TEMP_UUFAPPS.resolve(entryName);
                firstEntryName = entryName;
                if (Files.exists(appDirectory)) {
                    try {
                        FileUtils.deleteDirectory(appDirectory.toFile());
                    } catch (IOException e) {
                        throw new DeploymentException(
                                "Error occurred while deleting the directory, " + appDirectory.relativize(CARBON_HOME)
                                        + ".");
                    }
                    log.debug("Removed the existing folder which had the same name, " + entryName + "from "
                                      + DIR_TEMP_UUFAPPS.relativize(CARBON_HOME) + "directory.");
                }
            }
            if (zipEntry.isDirectory()) {
                createFile(unzipFolder, entryName);
                continue;
            }
            int hasParentDirectories = entryName.lastIndexOf(File.separatorChar);
            String directoryName = (hasParentDirectories == -1) ? null : entryName.substring(0, hasParentDirectories);
            if (directoryName != null) {
                createFile(unzipFolder, directoryName);
            }
            try (InputStream inputStream = zipFile.getInputStream(zipEntry);
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                         new FileOutputStream(DIR_TEMP_UUFAPPS.resolve(entryName).toFile()));) {
                int count;
                while ((count = inputStream.read()) != -1) {
                    bufferedOutputStream.write(count);
                }
            } catch (IOException e) {
                throw new DeploymentException(
                        "Error occurred extracting the zip file, " + file.getName() + " into " + DIR_TEMP_UUFAPPS
                                .relativize(CARBON_HOME) + "directory.");
            }
        }
        return DIR_TEMP_UUFAPPS.resolve(firstEntryName);
    }

    /**
     * Create a new folder
     *
     * @param parentDirectory Parent directory
     * @param path            Path to new folder
     */
    private void createFile(File parentDirectory, String path) {
        File file = new File(parentDirectory, path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new DeploymentException("Error encountered while creating a file at " + path + ".");
            }
        }
    }

    /**
     * Set app deployed base directories and returns the created application data object
     *
     * @param artifact Application content
     * @return Created appData object
     */
    private AppData getAppData(Artifact artifact) {
        App app;
        if (FilenameUtils.getExtension(artifact.getPath()).equals(ZIP_FILE_EXTENSION)) {
            //returns the application data when the artifact is a zip content
            app = appCreator.createApp(new ArtifactAppReference(unzip(artifact.getFile())));
            return new AppData(app, DIR_TEMP_UUFAPPS);
        } else {
            app = appCreator.createApp(new ArtifactAppReference(Paths.get(artifact.getPath())));
            return new AppData(app, DIR_UUFAPPS);
        }
    }

    /**
     * Returns the app name of a zip file
     *
     * @param file Zip file
     * @return App name
     */
    private String getZipFileName(File file) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            ZipEntry firstEntry = zipFile.stream().findFirst().orElseThrow(
                    (Supplier<RuntimeException>) () -> new DeploymentException(
                            "Cannot find app name for the zip file, " + file.getName()));
            return Paths.get(firstEntry.getName()).getFileName().toString();
        } catch (IOException e) {
            throw new DeploymentException("Error encountered while opening the zip file, " + file.getName() + ".", e);
        } finally {
            IOUtils.closeQuietly(zipFile);
        }
    }

    /**
     * Returns the app deployed base path, when app context path is given
     *
     * @param contextPath App context path
     * @return App deployed base path
     */
    @Override
    public Path getBasePath(String contextPath) {
        return deployedApps.get(contextPath).getBasePath();
    }

    @Override
    public Optional<App> getApp(String contextPath) {
        AppData appData = deployedApps.get(contextPath);
        if (appData != null) {
            return Optional.of(appData.getApp());
        } else {
            if (pendingToDeployArtifacts.containsKey(contextPath)) {
                return Optional.ofNullable(deployApp(contextPath));
            } else {
                return Optional.empty();
            }
        }
    }

    /**
     * This bind method is invoked by OSGi framework whenever a new RenderableCreator is registered.
     *
     * @param renderableCreator registered renderable creator
     */
    @Reference(name = "renderableCreator",
               service = RenderableCreator.class,
               cardinality = ReferenceCardinality.AT_LEAST_ONE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetRenderableCreator")
    public void setRenderableCreator(RenderableCreator renderableCreator) {
        if (!renderableCreators.add(renderableCreator)) {
            throw new IllegalArgumentException(
                    "A RenderableCreator for '" + renderableCreator.getSupportedFileExtensions() +
                            "' extensions is already registered");
        }
        log.info("RenderableCreator '" + renderableCreator.getClass().getName() + "' registered for " +
                         renderableCreator.getSupportedFileExtensions() + " extensions.");
    }

    /**
     * This bind method is invoked by OSGi framework whenever a RenderableCreator is left.
     *
     * @param renderableCreator unregistered renderable creator
     */
    public void unsetRenderableCreator(RenderableCreator renderableCreator) {
        renderableCreators.remove(renderableCreator);
        log.info("RenderableCreator " + renderableCreator.getClass().getName() + " unregistered for " +
                         renderableCreator.getSupportedFileExtensions() + " extensions.");
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        log.debug("ArtifactAppDeployer service activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        this.bundleContext = null;
        log.debug("ArtifactAppDeployer service deactivated.");
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        this.appCreator = new AppCreator(renderableCreators, classLoaderProvider);
        log.debug("AppCreator is ready.");

        bundleContext.registerService(Deployer.class, this, null);
        log.info("UUF ArtifactAppDeployer registered as a Carbon artifact deployer.");

        bundleContext.registerService(UUFAppRegistry.class, this, null);
        log.debug("ArtifactAppDeployer registered as an UUFAppRegistry.");
    }

    /**
     * Inner class to keep the app object and its base path
     */
    private static final class AppData {
        private final App app;
        private final Path basePath;

        private AppData(App app, Path basePath) {
            this.app = app;
            this.basePath = basePath;
        }

        /**
         * Returns app object
         *
         * @return App object
         */
        private App getApp() {
            return app;
        }

        /**
         * Returns base path, whether the app was deployed
         *
         * @return Base path
         */
        private Path getBasePath() {
            return basePath;
        }
    }
}
