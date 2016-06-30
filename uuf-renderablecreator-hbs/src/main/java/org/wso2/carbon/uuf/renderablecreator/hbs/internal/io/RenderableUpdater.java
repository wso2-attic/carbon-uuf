package org.wso2.carbon.uuf.renderablecreator.hbs.internal.io;

import com.github.jknack.handlebars.io.StringTemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.reference.FragmentReference;
import org.wso2.carbon.uuf.reference.LayoutReference;
import org.wso2.carbon.uuf.reference.PageReference;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.MutableHbsRenderable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedWatchServiceException;
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

public class RenderableUpdater {

    private static final Logger log = LoggerFactory.getLogger(RenderableUpdater.class);
    private final Set<Path> watchingDirectories;
    private final ConcurrentMap<Path, MutableHbsRenderable> watchingRenderables;
    private final WatchService watcher;
    private boolean isWatchServiceClosed;

    public RenderableUpdater() {
        this.watchingDirectories = new HashSet<>();
        this.watchingRenderables = new ConcurrentHashMap<>();
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new UUFException("Cannot create file watch service.", e);
        }
        this.isWatchServiceClosed = false;
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

    public void add(FragmentReference fragmentReference, MutableHbsRenderable fragmentRenderable) {
        Path componentPath = Paths.get(fragmentReference.getComponentReference().getPath());
        addToWatch(componentPath);
        Path fragmentAbsolutePath = Paths.get(fragmentReference.getRenderingFile().getAbsolutePath());
        watchingRenderables.put(componentPath.relativize(fragmentAbsolutePath), fragmentRenderable);
    }

    public void add(PageReference pageReference, MutableHbsRenderable pageRenderable) {
        Path componentAbsolutePath = Paths.get(pageReference.getComponentReference().getPath());
        addToWatch(componentAbsolutePath);
        Path pageAbsolutePath = Paths.get(pageReference.getRenderingFile().getAbsolutePath());
        watchingRenderables.put(componentAbsolutePath.relativize(pageAbsolutePath), pageRenderable);
    }

    public void add(LayoutReference layoutReference, MutableHbsRenderable layoutRenderable) {
        Path componentPath = Paths.get(layoutReference.getComponentReference().getPath());
        addToWatch(componentPath);
        Path layoutAbsolutePath = Paths.get(layoutReference.getRenderingFile().getAbsolutePath());
        watchingRenderables.put(componentPath.relativize(layoutAbsolutePath), layoutRenderable);
    }

    public void start() {
        if (isWatchServiceClosed) {
            throw new IllegalStateException("Cannot start RenderableUpdater as the file watch service is closed.");
        } else {
            new Thread(this::run).start();
        }
    }

    public void finish() {
        isWatchServiceClosed = true;
        try {
            watcher.close();
        } catch (IOException e) {
            log.warn("Cannot close file watch service.", e);
        }
    }

    private void run() {
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

            for (WatchEvent<?> event : watchKey.pollEvents()) {
                if (event.kind() != StandardWatchEventKinds.ENTRY_MODIFY) {
                    // We only watch file modify events.
                    continue;
                }
                @SuppressWarnings("unchecked")
                Path updatedFileRelativePath = ((WatchEvent<Path>) event).context();
                MutableHbsRenderable mutableRenderable = watchingRenderables.get(updatedFileRelativePath);
                if (mutableRenderable == null) {
                    // 'updatedFileRelativePath' does not represent a watching MutableHbsRenderable
                    continue;
                }
                String content;
                try {
                    content = new String(Files.readAllBytes(updatedFileRelativePath), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    log.error("Cannot read content of updated file '" + updatedFileRelativePath + "'.", e);
                    continue;
                }
                mutableRenderable.setTemplateSource(new StringTemplateSource(mutableRenderable.getPath(), content));
            }

            boolean valid = watchKey.reset();
            if (!valid) {
                break;
            }
        }
    }
}
