package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.create.ComponentReference;
import org.wso2.carbon.uuf.core.create.FileReference;
import org.wso2.carbon.uuf.core.create.FragmentReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ArtifactFragmentReference implements FragmentReference {
    private final Path path;
    private final ArtifactComponentReference component;
    private final ArtifactAppReference app;

    public ArtifactFragmentReference(Path path, ArtifactComponentReference component, ArtifactAppReference app) {
        this.path = path;
        this.component = component;
        this.app = app;
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public FileReference getChild(String name) {
        return new ArtifactFileReference(path.resolve(name), component, app);
    }

    @Override
    public Stream<FileReference> streamChildren() {
        try {
            return Files
                    .list(path)
                    .filter(Files::isRegularFile)
                    .map(f -> new ArtifactFileReference(f, component, app));
        } catch (IOException e) {
            throw new UUFException("Error while listing fragment children in " + path, e);
        }
    }

    @Override
    public ComponentReference getComponentReference() {
        return component;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
