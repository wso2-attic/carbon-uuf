package org.wso2.carbon.uuf.fileio;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.handlebars.Executable;
import org.wso2.carbon.uuf.handlebars.HbsPageRenderable;
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

    static HbsPageRenderable createRenderble(Path hbsFile) throws IOException {
        String content = new String(Files.readAllBytes(hbsFile), StandardCharsets.UTF_8);
        TemplateSource source = new StringTemplateSource(
                relativePath(hbsFile).toString(),
                content);
        return new HbsPageRenderable(source);
    }

    static Optional<Executable> createExecutable(Path jsFile) throws IOException {
        if (Files.isRegularFile(jsFile)) {

            Executable executable;
            executable = new JSExecutable(
                    new String(Files.readAllBytes(jsFile)),
                    FileUtil.relativePath(jsFile).toString());
            return Optional.of(executable);
        } else {
            return Optional.empty();
        }
    }

    static Map<String, Renderable> getBindings(Path bindingsConfig, Map<String, Fragment> fragments) throws IOException {
        Map<String, String> config = (Map<String, String>) new Yaml().load(Files.newInputStream(bindingsConfig));
        Map<String, Renderable> rv = new HashMap<>();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            Fragment fragment = fragments.get(entry.getValue());
            if (fragment == null) {
                throw new UUFException("Fragment '" + entry.getValue() + "' does not exists");
            }
            rv.put(entry.getKey(), fragment.getRenderer());
        }
        return rv;
    }
}
