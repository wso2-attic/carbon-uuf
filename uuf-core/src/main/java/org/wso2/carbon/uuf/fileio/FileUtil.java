package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

class FileUtil {

    static Path relativePath(Path path) {
        path = path.normalize();
        int pageIndex = -1;
        int count = path.getNameCount();
        for (int i = count; i >= 1; i--) {
            Path subPath = path.subpath(i - 1, i);
            if (subPath.toString().equals("pages") && i > 1 && pageIndex == -1) {
                pageIndex = i;
            }
            if (subPath.toString().equals("components") && i > 1) {
                return path.subpath(i - 2, count);
            }
        }
        if (pageIndex > -1) {
            return path.subpath(pageIndex - 2, count);
        }
        return path;
    }

    static Map<String, Renderable> getBindings(Path bindingsConfig, Map<String, Fragment> fragments)
            throws IOException {
        Map<String, Renderable> binding = new HashMap<>();
        if (Files.exists(bindingsConfig)) {
            Object yaml = new Yaml().load(Files.newInputStream(bindingsConfig));
            if (yaml != null && yaml instanceof Map) {
                //TODO: give proper error when val is a complex value
                @SuppressWarnings("unchecked")
                Map<String, String> config = (Map<String, String>) yaml;
                for (Map.Entry<String, String> entry : config.entrySet()) {
                    Fragment fragment = fragments.get(entry.getValue());
                    if (fragment == null) {
                        throw new UUFException(
                                "Fragment '" + entry.getValue() + "'  '" + bindingsConfig + "' does not exists");
                    }
                    binding.put(entry.getKey(), fragment.getRenderer());
                }
            }
        }
        return binding;
    }
}
