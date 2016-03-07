package org.wso2.carbon.uuf.artifact;

import org.wso2.carbon.uuf.core.Renderble;

import java.io.IOException;
import java.nio.file.Path;

class LayoutCreator {

    private Path componentsDir;

    public LayoutCreator(Path componentsDir) {
        this.componentsDir = componentsDir;
    }

    public Renderble createLayout(String layoutFullName, Path currentComponent) throws IOException {
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
        Path hbsFile = component.resolve("layouts").resolve(layoutName + ".hbs");
        return FileUtil.createRenderble(hbsFile);
    }
}
