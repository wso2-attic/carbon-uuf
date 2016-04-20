package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.create.AppReference;
import org.wso2.carbon.uuf.core.create.ComponentReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ArtifactAppReference implements AppReference {
    private Path path;

    public ArtifactAppReference(Path path) {
        this.path = path;
    }

    Path getPath() {
        return path;
    }

    @Override
    public ComponentReference getComponentReference(String componentSimpleName) {
        Path componentPath = path.resolve("components").resolve(componentSimpleName);
        return new ArtifactComponentReference(componentPath, this);
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public List<String> getDependencies() {
        try {
            return Files.readAllLines(path.resolve("components").resolve("dependency.tree"));
        } catch (IOException e) {
            throw new UUFException("Error while reading dependency.tree in " + path, e);
        }
    }
}
