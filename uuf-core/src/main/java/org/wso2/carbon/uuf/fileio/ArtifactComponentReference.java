package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.exception.UUFException;
import org.wso2.carbon.uuf.core.create.ComponentReference;
import org.wso2.carbon.uuf.core.create.FileReference;
import org.wso2.carbon.uuf.core.create.FragmentReference;
import org.wso2.carbon.uuf.core.create.PageReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class ArtifactComponentReference implements ComponentReference {
    private Path path;
    private ArtifactAppReference appReference;

    public ArtifactComponentReference(Path path, ArtifactAppReference appReference) {
        this.path = path;
        this.appReference = appReference;
    }

    @Override
    public Stream<PageReference> getPages(Set<String> supportedExtensions) {
        Path pages = path.resolve(DIR_NAME_PAGES);
        if (!Files.exists(pages)) {
            return Stream.<PageReference>empty();
        }
        try {
            return Files
                    .walk(pages)
                    .filter(path -> Files.isRegularFile(path) &&
                            supportedExtensions.contains(getExtension(path.getFileName().toString())))
                    .map(path -> new ArtifactPageReference(path, this, appReference));
        } catch (IOException e) {
            throw new UUFException("An error occurred while listing pages in '" + path + "'.", e);
        }
    }

    private String getExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex + 1);
    }

    @Override
    public Stream<FragmentReference> getFragments(Set<String> supportedExtensions) {
        Path fragments = path.resolve(DIR_NAME_FRAGMENTS);
        if (!Files.exists(fragments)) {
            return Stream.<FragmentReference>empty();
        }
        try {
            return Files.list(fragments)
                    .filter(Files::isDirectory)
                    .map(path -> new ArtifactFragmentReference(path, this, supportedExtensions));
        } catch (IOException e) {
            throw new UUFException("An error occurred while listing fragments in '" + path + "'.", e);
        }
    }

    @Override
    public Optional<FileReference> getBindingsConfig() {
        Path bindingsConfiguration = path.resolve(FILE_NAME_BINDINGS);
        if (Files.exists(bindingsConfiguration)) {
            return Optional.of(new ArtifactFileReference(bindingsConfiguration, this));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FileReference> getConfigurations() {
        Path configuration = path.resolve(FILE_NAME_CONFIGURATIONS);
        if (Files.exists(configuration)) {
            return Optional.of(new ArtifactFileReference(configuration, this));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FileReference> getOsgiImportsConfig() {
        Path binding = path.resolve(FILE_NAME_OSGI_IMPORTS);
        if (Files.exists(binding)) {
            return Optional.of(new ArtifactFileReference(binding, this));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public FileReference resolveLayout(String layoutName) {
        return new ArtifactFileReference(path.resolve(DIR_NAME_LAYOUTS).resolve(layoutName), this);
    }

    Path getPath() {
        return path;
    }

    ArtifactAppReference getAppReference() {
        return appReference;
    }
}
