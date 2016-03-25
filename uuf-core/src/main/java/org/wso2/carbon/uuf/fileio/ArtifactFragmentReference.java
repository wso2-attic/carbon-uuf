package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.create.FileReference;
import org.wso2.carbon.uuf.core.create.FragmentReference;

import java.nio.file.Path;

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
}
