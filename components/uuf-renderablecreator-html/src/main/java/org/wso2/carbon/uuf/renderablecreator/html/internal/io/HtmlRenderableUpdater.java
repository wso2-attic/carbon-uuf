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

package org.wso2.carbon.uuf.renderablecreator.html.internal.io;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.exception.FileOperationException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.renderablecreator.html.core.MutableHtmlRenderable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HtmlRenderableUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlRenderableUpdater.class);

    private final Set<Path> watchingDirectories;
    private final ConcurrentMap<Path, MutableHtmlRenderable> watchingRenderables;
    private final WatchService watchService;
    private final Thread watchServiceThread;
    private boolean isWatchServiceStopped;

    public HtmlRenderableUpdater() {
        this.watchingDirectories = new HashSet<>();
        this.watchingRenderables = new ConcurrentHashMap<>();
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new FileOperationException("Cannot create file watch service.", e);
        }
        this.watchServiceThread = new Thread(this::run, HtmlRenderableUpdater.class.getName() + "-WatchService");
        this.isWatchServiceStopped = false;
    }

    public void add(MutableHtmlRenderable mutableHtmlRenderable) {
        Path renderablePath = Paths.get(mutableHtmlRenderable.getAbsoluteFilePath());
        Path parentDirectory = renderablePath.getParent();
        if (watchingDirectories.add(parentDirectory)) {
            try {
                parentDirectory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (ClosedWatchServiceException e) {
                throw new UUFException("File watch service is closed.", e);
            } catch (NotDirectoryException e) {
                throw new FileOperationException("Cannot register path '" + parentDirectory +
                                                         "' to file watch service as it is not a directory.", e);
            } catch (IOException e) {
                throw new FileOperationException(
                        "An IO error occurred when registering path '" + parentDirectory + "' to file watch service.'",
                        e);
            }
        }
        watchingRenderables.put(renderablePath, mutableHtmlRenderable);
    }

    public void start() {
        if (isWatchServiceStopped) {
            throw new IllegalStateException("Cannot start HtmlRenderableUpdater as the file watch service is closed.");
        } else {
            watchServiceThread.start();
        }
    }

    public void finish() {
        isWatchServiceStopped = true;
        IOUtils.closeQuietly(watchService);
    }

    private void run() {
        while (!isWatchServiceStopped) {
            WatchKey watchKey;
            try {
                watchKey = watchService.take();
            } catch (ClosedWatchServiceException e) {
                LOGGER.debug("File watch service is closed.");
                return;
            } catch (InterruptedException e) {
                LOGGER.debug("File watch service interrupted.");
                return;
            }

            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                if (watchEvent.kind() != StandardWatchEventKinds.ENTRY_MODIFY) {
                    continue;
                }

                Path updatedDirectory = (Path) watchKey.watchable();
                @SuppressWarnings("unchecked")
                Path updatedFileName = ((WatchEvent<Path>) watchEvent).context();
                // Updating the changed html file
                Path updatedFileAbsolutePath = updatedDirectory.resolve(updatedFileName);
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(updatedFileAbsolutePath.getParent())) {
                    for (Path entry : stream) {
                        if (Files.isDirectory(entry)) {
                            continue;
                        }

                        MutableHtmlRenderable mutableHtmlRenderable = watchingRenderables.get(entry);
                        if (mutableHtmlRenderable != null) {
                            try {
                                String content = new String(Files.readAllBytes(entry), StandardCharsets.UTF_8);
                                mutableHtmlRenderable.setHtml(content);
                                LOGGER.info("HTML template '{}' reloaded successfully.", entry);
                            } catch (IOException e) {
                                LOGGER.error("An error occurred while reloading HTML template '{}'.", entry, e);
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("An error occurred while reloading HTML template '{}'.", updatedFileAbsolutePath, e);
                }
            }

            boolean valid = watchKey.reset();
            if (!valid) {
                // Watch key cannot not be reset because watch service is already closed.
                break;
            }
        }
    }
}
