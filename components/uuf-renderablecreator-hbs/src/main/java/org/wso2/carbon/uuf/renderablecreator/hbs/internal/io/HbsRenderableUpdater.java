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
 *
 *
 */

package org.wso2.carbon.uuf.renderablecreator.hbs.internal.io;

import com.github.jknack.handlebars.io.StringTemplateSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.reference.FileReference;
import org.wso2.carbon.uuf.api.reference.FragmentReference;
import org.wso2.carbon.uuf.api.reference.LayoutReference;
import org.wso2.carbon.uuf.api.reference.PageReference;
import org.wso2.carbon.uuf.exception.FileOperationException;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.MutableExecutable;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.MutableHbsRenderable;

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

public class HbsRenderableUpdater {

    private static final Logger log = LoggerFactory.getLogger(HbsRenderableUpdater.class);

    private final Set<Path> watchingDirectories;
    private final ConcurrentMap<Path, MutableHbsRenderable> watchingRenderables;
    private final ConcurrentMap<Path, MutableExecutable> watchingExecutables;
    private final WatchService watcher;
    private final Thread watchService;
    private boolean isWatchServiceStopped;

    public HbsRenderableUpdater() {
        this.watchingDirectories = new HashSet<>();
        this.watchingRenderables = new ConcurrentHashMap<>();
        this.watchingExecutables = new ConcurrentHashMap<>();
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new FileOperationException("Cannot create file watch service.", e);
        }
        this.watchService = new Thread(this::run, HbsRenderableUpdater.class.getName() + "-WatchService");
        this.isWatchServiceStopped = false;
    }

    public void add(LayoutReference layoutReference, MutableHbsRenderable mutableRenderable) {
        add(layoutReference.getRenderingFile(), mutableRenderable);
    }

    public void add(PageReference pageReference, MutableHbsRenderable mutableRenderable) {
        add(pageReference.getRenderingFile(), mutableRenderable);
    }

    public void add(FragmentReference fragmentReference, MutableHbsRenderable mutableRenderable) {
        add(fragmentReference.getRenderingFile(), mutableRenderable);
    }

    private void add(FileReference fileReference, MutableHbsRenderable mutableRenderable) {
        Path renderablePath = Paths.get(fileReference.getAbsolutePath());
        Path parentDirectory = renderablePath.getParent();
        if (watchingDirectories.add(parentDirectory)) {
            try {
                parentDirectory.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (ClosedWatchServiceException e) {
                throw new UUFException("File watch service is closed.", e);
            } catch (NotDirectoryException e) {
                throw new FileOperationException("Cannot register path '" + parentDirectory +
                                                         "' to file watch service as it is not a directory.", e);
            } catch (IOException e) {
                throw new FileOperationException("An IO error occurred when registering path '" + parentDirectory +
                                                         "' to file watch service.'", e);
            }
        }
        watchingRenderables.put(renderablePath, mutableRenderable);
        mutableRenderable.getMutableExecutable()
                .ifPresent(me -> watchingExecutables.put(Paths.get(me.getPath()), me));
    }

    public void start() {
        if (isWatchServiceStopped) {
            throw new IllegalStateException("Cannot start RenderableUpdater as the file watch service is closed.");
        } else {
            watchService.start();
        }
    }

    public void finish() {
        isWatchServiceStopped = true;
        IOUtils.closeQuietly(watcher);
    }

    private void run() {
        while (!isWatchServiceStopped) {
            WatchKey watchKey;
            try {
                watchKey = watcher.take();
            } catch (ClosedWatchServiceException e) {
                log.debug("File watch service is closed.");
                return;
            } catch (InterruptedException e) {
                log.debug("File watch service interrupted.");
                return;
            }

            for (WatchEvent<?> event : watchKey.pollEvents()) {
                if (event.kind() != StandardWatchEventKinds.ENTRY_MODIFY) {
                    continue; // We only watch file modify events.
                }

                Path updatedDirectory = (Path) watchKey.watchable();
                @SuppressWarnings("unchecked")
                Path updatedFileName = ((WatchEvent<Path>) event).context();
                Path updatedFileAbsolutePath = updatedDirectory.resolve(updatedFileName);
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(updatedFileAbsolutePath.getParent())) {
                    for (Path entry : stream) {
                        if (Files.isDirectory(entry)) {
                            continue;
                        }

                        MutableHbsRenderable mutableRenderable = watchingRenderables.get(entry);
                        if (mutableRenderable != null) {
                            // Updated file is a MutableHbsRenderable
                            try {
                                mutableRenderable.reload(new StringTemplateSource(mutableRenderable.getComponentPath(),
                                                                                  readFileContent(entry)));
                                log.info("Handlebars template '" + entry + "' reloaded successfully.");
                            } catch (IOException e) {
                                log.error("An error occurred while reloading Handlebars template '" + entry + "'.", e);
                            } catch (UUFException e) {
                                log.error("An error occurred while compiling Handlebars template '" + entry + "'.", e);
                            } catch (Exception e) {
                                log.error("An unexpected error occurred while reloading Handlebars template '" + entry +
                                        "'.", e);
                            }
                            continue;
                        }

                        MutableExecutable mutableExecutable = watchingExecutables.get(entry);
                        if (mutableExecutable != null) {
                            // Updated file is a MutableExecutable
                            try {
                                mutableExecutable.reload(readFileContent(entry));
                                log.info("JavaScript file '" + entry + "' reloaded successfully.");
                            } catch (IOException e) {
                                log.error("An error occurred while reloading JavaScript file '" + entry + "'.", e);
                            } catch (UUFException e) {
                                log.error("An error occurred while compiling JavaScript file '" + entry + "'.", e);
                            } catch (Exception e) {
                                log.error("An unexpected error occurred while reloading JavaScript file '" + entry +
                                        "'.", e);
                            }
                        }
                    }
                } catch (IOException e) {
                    log.error("An error occurred while reloading modified files '" + updatedFileAbsolutePath + "'.", e);
                }
            }

            boolean valid = watchKey.reset();
            if (!valid) {
                // Watch key cannot not be reset because watch service is already closed.
                break;
            }
        }
    }

    private static String readFileContent(Path filePath) throws IOException {
        return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
    }
}
