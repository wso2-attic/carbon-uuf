/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.uuf.internal.io.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.uuf.exception.FileOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipArtifactHandler {

    private static final String ZIP_FILE_EXTENSION = "zip";
    private static final Path CARBON_HOME = Utils.getCarbonHome();
    private static final Path TEMP_DIRECTORY = Paths.get(System.getProperty("java.io.tmpdir")).resolve("uufapps");
    private static final Logger log = LoggerFactory.getLogger(ZipArtifactHandler.class);

    public static boolean isZipArtifact(Artifact artifact) {
        return ZIP_FILE_EXTENSION.equals(FilenameUtils.getExtension(artifact.getPath()));
    }

    /**
     * @param zipArtifact zip app artifact
     * @return app name
     * @throws FileOperationException I/O error
     */
    public static String getAppName(Artifact zipArtifact) {
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipArtifact.getFile());
            ZipEntry firstEntry = zip.stream()
                    .findFirst()
                    .orElseThrow(() -> new FileOperationException("Cannot find app directory in zip artifact '" +
                                                                          zipArtifact.getPath() + "'."));
            if (firstEntry.isDirectory()) {
                return Paths.get(firstEntry.getName()).getFileName().toString();
            } else {
                throw new FileOperationException(
                        "Cannot find an app directory inside the zip artifact '" + zipArtifact.getPath() + "'.");
            }
        } catch (IOException e) {
            throw new FileOperationException(
                    "An error occurred when opening zip artifact '" + zipArtifact.getPath() + "'.", e);
        } finally {
            IOUtils.closeQuietly(zip);
        }
    }

    public static Path unzip(Artifact zipArtifact, String appName) {
        Path appDirectory = TEMP_DIRECTORY.resolve(appName);
        if (Files.exists(appDirectory)) {
            // A directory already exists in the tmp folder with the same app name, delete it before unzipping
            // the new app.
            try {
                FileUtils.deleteDirectory(appDirectory.toFile());
            } catch (IOException e) {
                throw new FileOperationException("Error occurred while deleting the directory, " +
                                                         appDirectory.relativize(CARBON_HOME) + ".");
            }
            log.debug("Removed the existing app directory '" + appDirectory.relativize(CARBON_HOME) +
                              "' before extracting app '" + appName + "'.");
        }

        ZipFile zip;
        try {
            zip = new ZipFile(zipArtifact.getFile(), ZipFile.OPEN_READ);
        } catch (IOException e) {
            throw new FileOperationException("Cannot open zip artifact '" + zipArtifact.getPath() + "' to extract.", e);
        }
        try {
            Enumeration<? extends ZipEntry> zipEntries = zip.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                if (zipEntry.isDirectory()) {
                    createDirectory(TEMP_DIRECTORY.resolve(zipEntry.getName()));
                } else {
                    Path tempFilePath = TEMP_DIRECTORY.resolve(zipEntry.getName());
                    // Here 'tempFilePath.getParent()' is never null.
                    createDirectory(tempFilePath.getParent());
                    try (InputStream inputStream = zip.getInputStream(zipEntry)) {
                        Files.copy(inputStream, tempFilePath);
                    } catch (IOException e) {
                        throw new FileOperationException(
                                "Cannot copy content of zip entry '" + zipEntry.getName() + "' in zip artifact '" +
                                        zipArtifact.getPath() + "' to temporary file '" + tempFilePath + "'.", e);
                    }
                }
            }
        } finally {
            // Close zip file.
            IOUtils.closeQuietly(zip);
        }

        return appDirectory;
    }

    private static void createDirectory(Path directoryPath) {
        try {
            Files.createDirectories(directoryPath);
        } catch (FileAlreadyExistsException e) {
            throw new FileOperationException("Cannot create directory '" + directoryPath +
                                                     "' as a file already exists in the same path.", e);
        } catch (IOException e) {
            throw new FileOperationException(
                    "An error occurred when creating directory '" + directoryPath + "'.", e);
        }
    }
}
