package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.create.AppReference;
import org.wso2.carbon.uuf.core.create.ComponentReference;
import org.wso2.carbon.uuf.core.create.FileReference;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ArtifactFileReference implements FileReference {

    private final Path path;
    private final ArtifactComponentReference component;
    private ArtifactAppReference app;

    ArtifactFileReference(Path path, ArtifactComponentReference component, ArtifactAppReference app) {
        this.path = path;
        this.component = component;
        this.app = app;
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public String getPathPattern() {
        StringBuilder sb = new StringBuilder();
        for (Path p : component.getPath().resolve("pages").relativize(path)) {
            sb.append('/');
            sb.append(p.toString());
        }
        return sb.toString();
    }

    @Override
    public String getContent() {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UUFException("Error while reading file " + path, e);
        }
    }

    @Override
    public String getRelativePath() {
        return app.getPath().relativize(path).toString();
    }

    @Override
    public Optional<FileReference> getSiblingIfExists(String name) {
        Path sibling = path.resolveSibling(name);
        if (Files.exists(sibling)) {
            return Optional.of(new ArtifactFileReference(sibling, component, app));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public ComponentReference getComponentReference() {
        return component;
    }

    @Override
    public AppReference getAppReference() {
        return component.getApp();
    }

    @Override
    public String getExtension() {
        String name = getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            return name.substring(lastDot + 1);
        }
        return "";
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
