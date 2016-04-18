package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.create.FileReference;
import org.wso2.carbon.uuf.core.create.PageReference;

public class ArtifactPageReference implements PageReference {

    private final String pathPattern;
    private final FileReference renderingFile;

    public ArtifactPageReference(String pathPattern, FileReference renderingFile) {
        this.pathPattern = pathPattern;
        this.renderingFile = renderingFile;
    }

    @Override
    public String getPathPattern() {
        return pathPattern;
    }

    @Override
    public FileReference getRenderingFile() {
        return renderingFile;
    }
}
