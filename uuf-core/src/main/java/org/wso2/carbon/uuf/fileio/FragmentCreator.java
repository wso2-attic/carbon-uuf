package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.UUFException;

import java.io.IOException;
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
            return new Fragment(name, fragmentDir.toString(), FileUtil.createRenderble(fragmentDir.resolve(
                    fileName + ".hbs")));
        } catch (IOException e) {
            throw new UUFException("error creating the fragment", e);
        }
    }
}
