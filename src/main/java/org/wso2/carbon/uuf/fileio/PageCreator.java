package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.Executable;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderble;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.UriPatten;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

class PageCreator {

    Page createPage(Path pageDir, LayoutCreator layoutCreator) {
        try {
            String name = pageDir.getFileName().toString();

            Renderble template;
            Renderble layout = null;
            Executable executable;

            Path hbsFile = pageDir.resolve(name + ".hbs");
            if (Files.isRegularFile(hbsFile)) {
                Path jsFile = pageDir.resolve(name + ".js");
                executable = FileUtil.createExecutable(jsFile);
                template = FileUtil.createRenderble(hbsFile);
                String layoutName = template.getLayoutName();
                if (layoutName != null) {
                    layout = layoutCreator.createLayout(layoutName, pageDir.getParent());
                }

                return new Page(getUriPatten(pageDir), template, executable, layout);
            } else {
                throw new UUFException("page must contain a template in '" + pageDir.toString() + "'");
            }
        } catch (IOException e) {
            // have to catch checked exception because we want to use it in a Stream mapping
            throw new UUFException("error creating the page", e);
        }
    }

    private UriPatten getUriPatten(Path pageDir) throws IOException {
        String name = pageDir.getFileName().toString();
        Path yamlFile = pageDir.resolve(name + ".yaml");
        String uri = null;
        if (Files.isRegularFile(yamlFile)) {
            Yaml yaml = new Yaml();
            Map map = (Map) yaml.load(Files.newBufferedReader(yamlFile));
            uri = (String) map.get("uri");
        }
        if (uri == null) {
            uri = "/" + name;
        }
        return new UriPatten(uri);
    }

}
