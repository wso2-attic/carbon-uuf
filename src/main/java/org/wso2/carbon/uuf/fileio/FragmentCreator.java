package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.UUFException;

import java.io.IOException;
import java.nio.file.Path;

public class FragmentCreator {
    public Fragment createFragment(Path fragmentDir) {
        String name = fragmentDir.getFileName().toString();
        try {
            return new Fragment(
                    name,
                    FileUtil.createRenderble(fragmentDir.resolve(name + ".hbs")),
                    FileUtil.createExecutable(fragmentDir.resolve(name + ".js"))

            );
        } catch (IOException e) {
            throw new UUFException("error creating the fragment", e);
        }
    }
}
