package org.wso2.carbon.uuf.fileio;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
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

        try {
            Path templateAbsolutePath = fragmentDir.resolve(fileName + ".hbs");
            if (!Files.exists(templateAbsolutePath)) {
                throw new UUFException("'" + fileName + "' fragment does not have a template.");
            }
            String templateString = new String(Files.readAllBytes(templateAbsolutePath), StandardCharsets.UTF_8);
            Path scriptPath = templateAbsolutePath.resolveSibling(name + ".js");
            TemplateSource templateSource = new StringTemplateSource(
                    FileUtil.relativePath(templateAbsolutePath).toString(),
                    templateString);
            HbsRenderable hbsRenderable = new HbsRenderable(
                    templateSource,
                    FileUtil.createScriptIfExist(scriptPath));
            return new Fragment(name, fragmentDir.toString(), hbsRenderable);
        } catch (IOException e) {
            throw new UUFException("Cannot create '" + name + "' fragment.");
        }
    }
}
