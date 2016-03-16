package org.wso2.carbon.uuf.fileio;

import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.handlebars.HbsPageRenderable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

class PageCreator {

    Page createPage(Path templateFile, LayoutCreator layoutCreator, Path components) {
        try {
            Renderable layout;
            Path templateFileAbsolute = components.resolve(templateFile);

            String name = templateFileAbsolute.getFileName().toString();
            int dotPos = name.lastIndexOf(".");
            if (dotPos >= 0) {
                name = name.substring(0, dotPos);
            }

            Path jsFile = templateFileAbsolute.getParent().resolve(name + ".js");
            HbsPageRenderable template = FileUtil.createRenderble(templateFileAbsolute);
            Optional<String> layoutName = template.getLayoutName();
            Map<String, Renderable> fillingZones;
            if (layoutName.isPresent()) {
                layout = layoutCreator.createLayout(layoutName.get(), templateFileAbsolute.getParent());
                fillingZones = template.getFillingZones();
            } else {
                layout = template;
                fillingZones = Collections.emptyMap();
            }

            return new Page(getUriPatten(templateFile, name), layout, fillingZones);
        } catch (IOException e) {
            // have to catch checked exception because we want to use it in a Stream mapping
            throw new UUFException("error creating the page", e);
        }
    }

    private UriPatten getUriPatten(Path pageDir, String name) throws IOException {
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
