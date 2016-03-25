package org.wso2.carbon.uuf.fileio;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.handlebars.Executable;
import org.wso2.carbon.uuf.handlebars.HbsRenderable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Deprecated
class LayoutCreator {

    private final Path componentsDir;

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
        Path layoutAbsolutePath = component.resolve("layouts").resolve(layoutName + ".hbs");
        String layoutString = new String(Files.readAllBytes(layoutAbsolutePath));
        TemplateSource layoutSource = new StringTemplateSource(
                FileUtil.relativePath(layoutAbsolutePath).toString(),
                layoutString);
        return new HbsRenderable(layoutSource, executable);
    }
}
