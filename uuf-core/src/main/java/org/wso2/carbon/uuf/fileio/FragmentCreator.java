package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.HbsRenderable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.wso2.carbon.uuf.fileio.FromArtifactAppCreator.ROOT_COMPONENT_NAME;

public class FragmentCreator {

    public Fragment createFragment(Path fragmentDir) {
        String fileName = fragmentDir.getFileName().toString();
        if (fileName.indexOf('.') > 0) {
            throw new UUFException("fragment names must not contain '.'");
        }
        String componentName = fragmentDir.getParent().getParent().getFileName().toString();
        String name;
        if (componentName.equals(ROOT_COMPONENT_NAME)) {
            name = fileName;
        } else {
            name = componentName + "." + fileName;
        }

        HbsRenderable hbsRenderable;
        try {
            Path templatePath = fragmentDir.resolve(fileName + ".hbs");
            if (!templatePath.toFile().exists()) {
                throw new UUFException("'" + fileName + "' fragment does not have a template.");
            }
            String templateSource = new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8);
            Path scriptPath = fragmentDir.resolve(fileName + ".js");
            if (scriptPath.toFile().exists()) {
                String scriptSource = new String(Files.readAllBytes(scriptPath), StandardCharsets.UTF_8);
                hbsRenderable = new HbsRenderable(templateSource, templatePath, scriptSource, scriptPath);
            } else {
                hbsRenderable = new HbsRenderable(templateSource, templatePath);
            }
        } catch (IOException e) {
            throw new UUFException("Cannot create '" + name + "' fragment.");
        }

        return new Fragment(name, fragmentDir.toString(), hbsRenderable);
    }
}
