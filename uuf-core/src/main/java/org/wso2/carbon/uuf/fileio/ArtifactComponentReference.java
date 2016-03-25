package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.create.AppReference;
import org.wso2.carbon.uuf.core.create.ComponentReference;
import org.wso2.carbon.uuf.core.create.FileReference;
import org.wso2.carbon.uuf.core.create.FragmentReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class ArtifactComponentReference implements ComponentReference {
    private Path path;
    private ArtifactAppReference app;

    public ArtifactComponentReference(Path path, ArtifactAppReference app) {
        this.path = path;
        this.app = app;
    }

    @Override
    public Stream<FileReference> streamPageFiles() {
        Path pages = path.resolve("pages");
        if (Files.exists(pages)) {
            try {
                return Files.walk(pages).map(path -> new ArtifactFileReference(path, this, app));
            } catch (IOException e) {
                throw new UUFException("Error while finding pages in " + path, e);
            }
        } else {
            return Stream.empty();
        }
    }

    @Override
    public Stream<FragmentReference> streamFragmentFiles() {
        try {
            Path fragments = path.resolve("fragments");
            if (Files.exists(fragments)) {
                return Files.list(fragments).map(path -> new ArtifactFragmentReference(path, this, app));
            } else {
                return Stream.empty();
            }
        } catch (IOException e) {
            throw new UUFException("Error while listing fragments in " + path, e);
        }
    }

    @Override
    public FileReference resolveLayout(String layoutName) {
        return new ArtifactFileReference(path.resolve("layouts").resolve(layoutName), this, app);
    }


    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public String getContext() {
        String name = this.getName();
        return "/" + (name.equals("root") ? "" : getContextFormName(name));
    }

    private String getContextFormName(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            return name.substring(lastDot + 1);
        } else {
            return name;
        }
    }

    @Override
    public String getVersion() {
        //TODO: read this from path config
        return "1.0.0";
    }

    Path getPath() {
        return path;
    }

    @Override
    public AppReference getApp() {
        return app;
    }

    @Override
    public Optional<FileReference> getConfig() {
        Path binding = path.resolve("config.yaml");
        if (Files.exists(binding)) {
            return Optional.of(new ArtifactFileReference(binding, this, app));
        } else {
            return Optional.empty();
        }
    }
}
