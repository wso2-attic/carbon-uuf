package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.create.AppReference;
import org.wso2.carbon.uuf.core.create.ComponentReference;
import org.wso2.carbon.uuf.core.exception.UUFException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ArtifactAppReference implements AppReference {

    private final Path path;

    public ArtifactAppReference(Path path) {
        this.path = path;
    }

    Path getPath() {
        return path;
    }

    @Override
    public ComponentReference getComponentReference(String componentSimpleName) {
        Path componentPath = path.resolve(DIR_NAME_COMPONENTS).resolve(componentSimpleName);
        return new ArtifactComponentReference(componentPath);
    }

    @Override
    public String getName() {
        Path fileName = path.getFileName();
        return (fileName == null) ? "" : fileName.toString();
    }

    @Override
    public List<String> getDependencies() {
        Path dependencyTreeFile = path.resolve(DIR_NAME_COMPONENTS).resolve(FILE_NAME_DEPENDENCY_TREE);
        try {
            return Files.readAllLines(dependencyTreeFile);
        } catch (IOException e) {
            throw new UUFException(
                    "An error occurred while reading dependencies from file '" + dependencyTreeFile + "'.", e);
        }
    }
}
