package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.Executable;
import org.wso2.carbon.uuf.handlebars.JSExecutable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        Optional<Map> config = readYamlConfig(bindingsConfig);
        if (config.isPresent()) {
            @SuppressWarnings("unchecked") Map<String, String> configuration = config.get();
            for (Map.Entry<String, String> entry : configuration.entrySet()) {
                Fragment fragment = fragments.get(entry.getValue());
                if (fragment == null) {
                    throw new UUFException(
                            "Fragment '" + entry.getValue() + "'  '" + bindingsConfig + "' does not exists");
                }
                binding.put(entry.getKey(), fragment.getRenderer());
            }
        }
        return binding;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getConfiguration(Path appConfig) throws IOException {
        Optional<Map> config = readYamlConfig(appConfig);
        return (config.isPresent()) ? (Map<String, String>) config.get() : new HashMap<>();
    }

    private static Optional<Map> readYamlConfig(Path configPath) throws IOException {
        if (!Files.exists(configPath)) {
            return Optional.empty();
        }
        Map config = new Yaml().loadAs(Files.newInputStream(configPath), Map.class);
        return (config != null) ? Optional.of(config) : Optional.empty();
    }

    public static Optional<Executable> createScriptIfExist(Path scriptPath) throws IOException {
        if (scriptPath.toFile().exists()) {
            String scriptSource = new String(Files.readAllBytes(scriptPath), StandardCharsets.UTF_8);
            String path = relativePath(scriptPath).toString();
            JSExecutable script = new JSExecutable(scriptSource, Optional.of(path));
            return Optional.of(script);
        } else {
            return Optional.empty();
        }
    }
}
