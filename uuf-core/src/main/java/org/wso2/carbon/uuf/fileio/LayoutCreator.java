package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.handlebars.Executable;
import org.wso2.carbon.uuf.handlebars.HbsRenderable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

class LayoutCreator {

    private Path componentsDir;

    public LayoutCreator(Path componentsDir) {
        this.componentsDir = componentsDir;
    }

    public Renderable createLayout(String layoutFullName, Path currentComponent, Optional<Executable> executable)
            throws IOException {
        Path component;
        String layoutName;
        int lastDot = layoutFullName.lastIndexOf('.');
        if (lastDot >= 0) {
            String componentName = layoutFullName.substring(0, lastDot);
            layoutName = layoutFullName.substring(lastDot + 1);
            component = componentsDir.resolve(componentName);
        } else {
            component = currentComponent;
            layoutName = layoutFullName;
        }
        Path layoutPath = component.resolve("layouts").resolve(layoutName + ".hbs");
        String layoutSource = new String(Files.readAllBytes(layoutPath));
        if (executable.isPresent()) {
            return new HbsRenderable(layoutSource, layoutPath, executable.get());
        } else {
            return new HbsRenderable(layoutSource, layoutPath);
        }
    }
}
