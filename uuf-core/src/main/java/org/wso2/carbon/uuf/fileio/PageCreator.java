package org.wso2.carbon.uuf.fileio;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.handlebars.HbsPageRenderable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

class PageCreator {

    Page createPage(Path templatePath, LayoutCreator layoutCreator, Path components) {
        try {
            Path templateAbsolutePath = components.resolve(templatePath);
            String name = templateAbsolutePath.getFileName().toString();
            int dotPos = name.lastIndexOf(".");
            if (dotPos >= 0) {
                name = name.substring(0, dotPos);
            }
            String templateString = new String(Files.readAllBytes(templateAbsolutePath), StandardCharsets.UTF_8);
            Path scriptPath = templateAbsolutePath.resolveSibling(name + ".js");
            TemplateSource templateSource = new StringTemplateSource(
                    FileUtil.relativePath(templateAbsolutePath).toString(),
                    templateString);
            HbsPageRenderable hbsRenderable = new HbsPageRenderable(
                    templateSource,
                    FileUtil.createScriptIfExist(scriptPath));

            Optional<String> layoutName = hbsRenderable.getLayoutName();
            Renderable layout;
            Map<String, HbsPageRenderable> fillingZones = hbsRenderable.getFillingZones();
            if (layoutName.isPresent()) {
                layout = layoutCreator.createLayout(
                        layoutName.get(),
                        templateAbsolutePath.getParent(),
                        hbsRenderable.getScript());
            } else {
                layout = hbsRenderable;
            }

            return new Page(getUriPatten(templatePath, name), layout, fillingZones);
        } catch (IOException e) {
            // have to catch checked exception because we want to use it in a Stream mapping
            throw new UUFException("error creating the page", e);
        }
    }


    private UriPatten getUriPatten(Path pageDir, String name) {
        StringBuilder uri = new StringBuilder();
        for (int i = 2; i < pageDir.getNameCount() - 1; i++) {
            Path aPageDir = pageDir.getName(i);
            uri.append("/");
            uri.append(aPageDir.toString());
        }
        uri.append("/");
        if (!name.equals("index")) {
            uri.append(name);
        }
        return new UriPatten(uri.toString());
    }

}
