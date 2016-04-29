package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.create.FileReference;
import org.wso2.carbon.uuf.core.exception.UUFException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ArtifactFileReference implements FileReference {

    private final Path path;
    private final ArtifactComponentReference componentReference;

    ArtifactFileReference(Path path, ArtifactComponentReference componentReference) {
        this.path = path;
        this.componentReference = componentReference;
    }

    @Override
    public String getName() {
        Path fileName = path.getFileName();
        return (fileName == null) ? "" : fileName.toString();
    }

    @Override
    public String getExtension() {
        String fileName = getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex + 1);
    }

    @Override
    public String getContent() {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UUFException("Error while reading from file in '" + path + "'.", e);
        }
    }

    @Override
    public String getRelativePath() {
        return componentReference.getPath().relativize(path).toString();
    }

    @Override
    public String getAbsolutePath() {
        try {
            return path.toRealPath().toString();
        } catch (IOException e) {
            return path.toAbsolutePath().toString();
        }
    }

    @Override
    public Optional<FileReference> getSibling(String name) {
        Path sibling = path.resolveSibling(name);
        return Files.exists(sibling) ? Optional.of(new ArtifactFileReference(sibling, componentReference)) :
                Optional.<FileReference>empty();
    }
}
