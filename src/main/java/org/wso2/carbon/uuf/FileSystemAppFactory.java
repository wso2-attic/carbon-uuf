package org.wso2.carbon.uuf;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.Executable;
import org.wso2.carbon.uuf.core.HandlebarsRenderble;
import org.wso2.carbon.uuf.core.JSExecutable;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderble;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.core.util.FileUtil;
import org.yaml.snakeyaml.Yaml;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSystemAppFactory implements AppFactory {

    private static final Logger log = LoggerFactory.getLogger(FileSystemAppFactory.class);
    private final String[] paths;

    public FileSystemAppFactory(String[] paths) {
        this.paths = paths;
    }

    private App createFromComponents(Path components, String context) throws IOException {
        List<Page> pages = new ArrayList<>();
        Map<String, Renderble> layouts = new HashMap<>();

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(components)) {
            for (Path component : dirStream)
                if (Files.isDirectory(component)) {
                    createLayouts(component.resolve("layouts"), layouts, component.getFileName().toString());
                } else {
                    log.warn("component must be a directory " + component + "'");
                }
        }
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(components)) {
            for (Path component : dirStream)
                if (Files.isDirectory(component)) {
                    createPages(component.resolve("pages"), pages, layouts);
                } else {
                    log.warn("component must be a directory " + component + "'");
                }
        }
        return new App(context, pages, Collections.emptyList());
    }

    private void createLayouts(Path layoutsDir, Map<String, Renderble> layouts, String componentName) throws IOException {
        if (Files.isDirectory(layoutsDir)) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(layoutsDir)) {
                for (Path layoutDir : dirStream) {
                    String fileName = layoutDir.getFileName().toString();
                    if (Files.isRegularFile(layoutDir) && fileName.endsWith(".hbs")) {
                        layouts.put(componentName + "." + fileName.substring(0, fileName.length() - 4), createLayout(layoutDir));
                    } else {
                        log.warn("layout must be a hbs file " + layoutDir + "'");
                    }
                }
            }

        } else {
            if (Files.exists(layoutsDir)) {
                log.warn("layouts must be a directory " + layoutsDir + "'");
            }
        }

    }

    private Renderble createLayout(Path hbsFile) {
        try {
            TemplateSource source = new StringTemplateSource(
                    FileUtil.relativePath(hbsFile).toString(),
                    new String(Files.readAllBytes(hbsFile)));
            return new HandlebarsRenderble(source);
        } catch (IOException e) {
            throw new UUFException(
                    "error creating the  '" + hbsFile.toString() + "'",
                    e);

        }
    }

    private void createPages(Path pagesDir, List<Page> pages, Map<String, Renderble> layouts) throws IOException {
        if (Files.isDirectory(pagesDir)) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(pagesDir)) {
                for (Path pageDir : dirStream) {
                    if (Files.isDirectory(pageDir)) {
                        pages.add(createPage(pageDir, layouts));
                    } else {
                        log.warn("page must be a directory " + pageDir.toString() + "'");
                    }
                }
            }

        } else {
            if (Files.exists(pagesDir)) {
                log.warn("pages must be a directory " + pagesDir.toString() + "'");
            }
        }
    }

    private Page createPage(Path pageDir, Map<String, Renderble> layouts) throws IOException {
        String name = pageDir.getFileName().toString();

        Renderble template;
        Renderble layout;
        Executable executable = null;

        Path hbsFile = pageDir.resolve(name + ".hbs");
        if (Files.isRegularFile(hbsFile)) {
            Path jsFile = pageDir.resolve(name + ".js");
            if (Files.isRegularFile(jsFile)) {
                executable = new JSExecutable(
                        new String(Files.readAllBytes(jsFile)),
                        FileUtil.relativePath(jsFile).toString());
            }
            String content = new String(Files.readAllBytes(hbsFile), StandardCharsets.UTF_8);
            TemplateSource source = new StringTemplateSource(
                    FileUtil.relativePath(hbsFile).toString(),
                    content);
            template = new HandlebarsRenderble(source);
            layout = layouts.get(template.getLayoutName());
        } else {
            throw new UUFException("page must contain a template in '" + pageDir.toString() + "'");
        }

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

        return new Page(new UriPatten(uri), template, executable, layout);
    }


    @Override
    public App createApp(String name, String context) {
        //app list mush be <white-space> and comma separated. <white-space> in app names not allowed
        for (String pathString : paths) {
            Path path = FileSystems.getDefault().getPath(pathString);
            if (name.equals(path.toAbsolutePath().normalize().getFileName().toString())) {
                try {
                    return createFromComponents(path.resolve("components"), context);
                } catch (IOException e) {
                    throw new UUFException("error while creating app for '" + name + "'",
                            e);
                }
            }
        }
        throw new UUFException("app by the name '" + name + "' is not found in " + Arrays.toString(paths),
                Response.Status.NOT_FOUND);
    }
}
