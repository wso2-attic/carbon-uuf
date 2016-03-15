package org.wso2.carbon.uuf.fileio;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.AppCreator;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;

import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FromArtifactAppCreator implements AppCreator {

    public static final String ROOT_COMPONENT_NAME = "ROOT";

    private final String[] paths;
    private final PageCreator pageCreator = new PageCreator();
    private final FragmentCreator fragmentCreator = new FragmentCreator();

    public FromArtifactAppCreator(String[] paths) {
        this.paths = paths;
    }

    private static Stream<Path> subDirsOfAComponent(Path componentDir, String dirName) {
        try {
            Path pagesDir = componentDir.resolve(dirName);
            if (Files.isDirectory(pagesDir)) {
                return Files.list(pagesDir);
            } else {
                return Stream.empty();
            }
        } catch (IOException e) {
            throw new UUFException("error while finding the pages of " + componentDir, e);
        }
    }

    private static Stream<? extends Path> findHbs(Path path, Path components) {
        try {
            return Files.find(path, Integer.MAX_VALUE, (p, a) -> p.getFileName().toString().endsWith(".hbs"))
                    .map(components::relativize);
        } catch (IOException e) {
            throw new UUFException("error while finding a page", e);
        }
    }

    private App createFromComponents(Path components, String context) throws IOException {
        if (!Files.exists(components)) {
            throw new FileNotFoundException("components dir must exist in a build artifact");
        }

        LayoutCreator layoutCreator = new LayoutCreator(components);
        List<Page> pages = Files.list(components).flatMap(c -> subDirsOfAComponent(c, "pages")).flatMap(
                p -> findHbs(p, components)).parallel().map(
                p -> pageCreator.createPage(p, layoutCreator, components)).collect(Collectors.toList());

       Map<String,Fragment> fragments = Files
                .list(components)
                .flatMap(c -> subDirsOfAComponent(c, "fragments"))
                .parallel()
                .map(fragmentCreator::createFragment)
                .collect(Collectors.toMap( Fragment::getName, Function.identity()));
        Path bindingsConfig = components.resolve("bindings.yaml");
        Map<String, Renderable> bindings = FileUtil.getBindings(bindingsConfig, fragments);
        return new App(context, pages, fragments, bindings);
    }

    @Override
    public App createApp(String name, String context) {
        try {
            return createFromComponents(getAppPath(name).resolve("components"), context);
        } catch (IOException e) {
            throw new UUFException("error while creating app for '" + name + "'", e);
        }
    }

    @Override
    public Path resolve(String appName, String resourcePath) {
        // component_resource_path: /public/<component_name>/base/js/lib.js
        // fragment_resource_path: /public/<component_name>/<fragment_name>/js/lib.js
        // root_resource_path: /public/root/base/js/lib.js
        // root_fragment_path: /public/root/<fragment_name>/js/lib.js

        String filePath = "";
        Path appPath = getAppPath(appName);
        String resourcePathParts[] = resourcePath.split("/");

        int thirdSlash = StringUtils.ordinalIndexOf(resourcePath, "/", 3);
        String subResourcePath = resourcePath.substring(thirdSlash, resourcePath.length());

        if (resourcePathParts[0].equals(AppCreator.STATIC_RESOURCE_PREFIX)) {
            if (resourcePathParts[1].equals(AppCreator.STATIC_RESOURCE_PATH_PARAM_ROOT)) {
                if (resourcePathParts[2].equals(AppCreator.STATIC_RESOURCE_PATH_PARAM_BASE)) {
                    //root_resource_path
                    appPath.resolve(subResourcePath);
                } else {
                    //root_fragment_path
                    appPath.resolve(subResourcePath);
                }
            } else {
                if (resourcePathParts[2].equals(AppCreator.STATIC_RESOURCE_PATH_PARAM_BASE)) {
                    //component_resource_path
                    appPath.resolve(subResourcePath);
                } else {
                    //fragment_resource_path
                    appPath.resolve(subResourcePath);
                }
            }
        }

        throw new UUFException("Resource by uri '" + resourcePath + "' is not found in " + appName,
                Response.Status.NOT_FOUND);
    }

    private Path getAppPath(String name) {
        // app list mush be <white-space> and comma separated. <white-space> in app names not allowed
        for (String pathString : paths) {
            Path path = FileSystems.getDefault().getPath(pathString).toAbsolutePath().normalize();
            if (name.equals(path.getFileName().toString())) {
                return path;
            }
        }
        throw new UUFException("app by the name '" + name + "' is not found in " + Arrays.toString(paths),
                Response.Status.NOT_FOUND);
    }
}
