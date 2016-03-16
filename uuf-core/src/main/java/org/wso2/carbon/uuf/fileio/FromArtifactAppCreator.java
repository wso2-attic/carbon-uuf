package org.wso2.carbon.uuf.fileio;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.AppCreator;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;

import javax.ws.rs.core.Response;
import java.io.File;
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

    public static final String ROOT_COMPONENT_NAME = "root";

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

    /**
     * This method resolves static routing request uris. URI types categorized into;
     * <ul>
     *     <li>root_resource_uri: /public/root/base/{subResourceUri}</li>
     *     <li>root_fragment_uri: /public/root/{fragmentName}/{subResourceUri}</li>
     *     <li>component_resource_uri: /public/{componentName}/base/{subResourceUri}</li>
     *     <li>fragment_resource_uri: /public/{componentName}/{fragmentName}/{subResourceUri}</li>
     * </ul>
     * These path types are mapped into following file paths on the file system;
     * <ul>
     *      <li>{appName}/components/[{componentName}|ROOT]/[{fragmentName}|base]/public/{subResourcePath}</li>
     * </ul>
     * @param appName application name
     * @param resourcePath resource uri
     * @return resolved path
     */
    @Override
    public Path resolve(String appName, String resourcePath) {
        Path appPath = getAppPath(appName);
        String resourcePathParts[] = resourcePath.split("/");

        int fourthSlash = StringUtils.ordinalIndexOf(resourcePath, "/", 4);
        String subResourcePath = resourcePath.substring(fourthSlash, resourcePath.length());

        String resourceUriPart = resourcePathParts[1];
        String componentUriPart = resourcePathParts[2];
        String fragmentUriPart = resourcePathParts[3];

        if (resourceUriPart.equals(AppCreator.STATIC_RESOURCE_PREFIX)) {
            throw new IllegalArgumentException("resourcePath should starts with `/public`!");
        }

        Path componentPath = appPath.resolve(componentUriPart);
        Path fragmentPath;
        if (fragmentUriPart.equals(AppCreator.STATIC_RESOURCE_PATH_PARAM_BASE)) {
            fragmentPath = componentPath;
        } else {
            fragmentPath = componentPath.resolve(fragmentUriPart);
        }
        //{appName}/components/[{componentName}|ROOT]/[{fragmentName}|base]/public/{subResourcePath}
        return fragmentPath.resolve("public").resolve(subResourcePath);
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
