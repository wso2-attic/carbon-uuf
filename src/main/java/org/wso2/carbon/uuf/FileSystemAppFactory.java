package org.wso2.carbon.uuf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.*;
import org.yaml.snakeyaml.Yaml;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileSystemAppFactory implements AppFactory {

    private final String[] paths;
    private static final Logger log = LoggerFactory.getLogger(FileSystemAppFactory.class);
    private Yaml yaml;

    public FileSystemAppFactory(String[] paths) {
        this.paths = paths;
    }

    public App createFromComponents(Path components, String context) throws IOException {
        List<Page> pages = new ArrayList<>();

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(components)) {
            for (Path component : dirStream) {
                if (Files.isDirectory(component)) {
                    createPages(component, pages);
                } else {
                    log.warn("component must be a directory " + component.toString() + "'");
                }
            }
        }
        return new App(context, pages, Collections.emptyList());
    }

    private void createPages(Path component, List<Page> pages) throws IOException {
        Path pagesDir = component.resolve("pages");
        if (Files.isDirectory(pagesDir)) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(pagesDir)) {
                for (Path pageDir : dirStream) {
                    if (Files.isDirectory(pageDir)) {
                        String name = pageDir.getFileName().toString();

                        Renderble template;
                        Executable executable = null;

                        Path hbsFile = pageDir.resolve(name + ".hbs");
                        if (Files.isRegularFile(hbsFile)) {
                            Path jsFile = pageDir.resolve(name + ".js");
                            if (Files.isRegularFile(jsFile)) {
                                executable = new JSExecutable(
                                        new String(Files.readAllBytes(jsFile)),
                                        Util.relativePath(jsFile).toString());
                            }
                            //TODO: use UTF-8
                            template = new HandlebarsRenderble(
                                    new String(Files.readAllBytes(hbsFile)),
                                    Util.relativePath(hbsFile).toString());
                        } else {
                            throw new UUFException(
                                    "page must contain a template in '" + pageDir.toString() + "'",
                                    Response.Status.INTERNAL_SERVER_ERROR);
                        }

                        Path yamlFile = pageDir.resolve(name + ".yaml");
                        String uri = null;
                        if (Files.isRegularFile(yamlFile)) {
                            this.yaml = new Yaml();
                            Map map = (Map) yaml.load(Files.newBufferedReader(yamlFile));
                            uri = (String) map.get("uri");
                        }
                        if (uri == null) {
                            uri = "/" + name;
                        }

                        pages.add(new Page(new UriPatten(uri), template, executable));
                    } else {
                        log.warn("page must be a directory " + component.toString() + "'");
                    }
                }
            }

        } else {
            if (Files.exists(pagesDir)) {
                log.warn("pages must be a directory " + pagesDir.toString() + "'");
            }
        }
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
                            Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
        }
        throw new UUFException("app by the name '" + name + "' is not found in " + Arrays.toString(paths),
                Response.Status.NOT_FOUND);
    }
}
