package org.wso2.carbon.uuf.renderablecreator.hbs.internal.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.reference.PageReference;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.MutableHbsRenderable;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Set;

public class RenderableUpdater implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RenderableUpdater.class);
    private final Set<Path> watchingDirectories;
    private final WatchService watcher;
    private boolean isWatchServiceClosed;

    public RenderableUpdater() {
        this.watchingDirectories = new HashSet<>();
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new UUFException("Cannot create file watch service.", e);
        }
    }

    private void addToWatch(Path componentPath) {
        if (watchingDirectories.add(componentPath)) {
            try {
                componentPath.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (ClosedWatchServiceException e) {
                throw new UUFException("File watch service is closed.", e);
            } catch (NotDirectoryException e) {
                throw new UUFException(
                        "Cannot register file watch service for path'" + componentPath + "' as it is not a directory.",
                        e);
            } catch (IOException e) {
                throw new UUFException(
                        "An IO error occurred when registering file watch service for path '" + componentPath + "'.",
                        e);
            }
        }
    }

    public void add(PageReference pageReference, MutableHbsRenderable pageRenderable) {
        Path componentPath = Paths.get(pageReference.getComponentReference().getPath());
        addToWatch(componentPath);
        Path pageRelativePath = componentPath.relativize(Paths.get(pageReference.getRenderingFile().getAbsolutePath()));
    }

    public void start() {
        isWatchServiceClosed = false;
        new Thread(this).start();
    }

    public void finish() {
        isWatchServiceClosed = true;
        try {
            watcher.close();
        } catch (IOException e) {
            log.warn("Cannot close file watch service.", e);
        }
    }

    @Override
    public void run() {
        while (!isWatchServiceClosed) {
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

            for (WatchEvent<?> rawEvent : watchKey.pollEvents()) {
                @SuppressWarnings("unchecked")
                WatchEvent<Path> event = (WatchEvent<Path>) rawEvent;

                WatchEvent.Kind<?> eventKind = event.kind();
                if (eventKind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    Path updatedFileRelativePath = event.context();
                    // update renderable
                }
            }

            boolean valid = watchKey.reset();
            if (!valid) {
                break;
            }
        }
    }
}
