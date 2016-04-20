package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.create.FileReference;
import org.wso2.carbon.uuf.core.create.FragmentReference;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class ArtifactFragmentReference implements FragmentReference {
    private final Path path;
    private final ArtifactComponentReference componentReference;
    private final Set<String> supportedExtensions;

    public ArtifactFragmentReference(Path path, ArtifactComponentReference componentReference,
                                     Set<String> supportedExtensions) {
        this.path = path;
        this.componentReference = componentReference;
        this.supportedExtensions = supportedExtensions;
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public FileReference getRenderingFile() {
        String fragmentName = getName();
        for (String extension : supportedExtensions) {
            Path renderingFilePath = path.resolve(fragmentName + "." + extension);
            if (Files.isRegularFile(renderingFilePath)) {
                return new ArtifactFileReference(renderingFilePath, componentReference);
            }
        }
        throw new UUFException("Fragment '" + fragmentName + "' of component '" + componentReference.getPath() +
                                       "' is empty.");
    }
}
