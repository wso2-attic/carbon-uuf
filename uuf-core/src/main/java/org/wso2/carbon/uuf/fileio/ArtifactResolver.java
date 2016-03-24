package org.wso2.carbon.uuf.fileio;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.uuf.core.Resolver;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.create.ComponentReference;
import org.wso2.carbon.uuf.core.create.FileReference;
import org.wso2.carbon.uuf.core.create.FragmentReference;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ArtifactResolver implements Resolver {
    private final List<Path> paths;

    public ArtifactResolver(List<Path> paths) {
        this.paths = paths;
    }


    private Path getAppPath(String name) {
        // app list mush be <white-space> and comma separated. <white-space> in app names not allowed
        for (Path uufAppPath : paths) {
            Path path = uufAppPath.toAbsolutePath().normalize();
            if (name.equals(path.getFileName().toString())) {
                return path;
            }
        }
        throw new UUFException("app by the name '" + name + "' is not found!",
                Response.Status.NOT_FOUND);
    }

    /**
     * This method resolves static routing request uris. URI types categorized into;
     * <ul>
     * <li>root_resource_uri: /public/root/base/{subResourceUri}</li>
     * <li>root_fragment_uri: /public/root/{fragmentName}/{subResourceUri}</li>
     * <li>component_resource_uri: /public/{componentName}/base/{subResourceUri}</li>
     * <li>fragment_resource_uri: /public/{componentName}/{fragmentName}/{subResourceUri}</li>
     * </ul>
     * These path types are mapped into following file paths on the file system;
     * <ul>
     * <li>{appName}/components/[{componentName}|ROOT]/[{fragmentName}|base]/public/{subResourcePath}</li>
     * </ul>
     *
     * @param appName      application name
     * @param resourcePath resource uri
     * @return resolved path
     */
    @Override
    public Path resolveStatic(String appName, String resourcePath) {
        Path appPath = getAppPath(appName);
        String resourcePathParts[] = resourcePath.split("/");

        if (resourcePathParts.length < 5) {
            throw new IllegalArgumentException("Invalid resourcePath! `" + resourcePath + "`");
        }

        String resourceUriPrefixPart = resourcePathParts[1];
        String componentUriPart = resourcePathParts[2];
        String fragmentUriPart = resourcePathParts[3];
        int fourthSlash = StringUtils.ordinalIndexOf(resourcePath, "/", 4);
        String subResourcePath = resourcePath.substring(fourthSlash + 1, resourcePath.length());

        if (!resourceUriPrefixPart.equals(STATIC_RESOURCE_URI_PREFIX)) {
            throw new IllegalArgumentException("Resource path should starts with `/public`!");
        }

        Path componentPath = appPath.resolve("components").resolve(componentUriPart);
        Path fragmentPath;
        if (fragmentUriPart.equals(STATIC_RESOURCE_URI_BASE_PREFIX)) {
            fragmentPath = componentPath;
        } else {
            fragmentPath = componentPath.resolve("fragments").resolve(fragmentUriPart);
        }

        return fragmentPath.resolve("public").resolve(subResourcePath);
    }

    private FragmentReference createFragmentReference(Path path, Path component, Path app) {
        return new FragmentReference() {
            @Override
            public String getName() {
                return path.getFileName().toString();
            }

            @Override
            public FileReference getChild(String name) {
                return createFileReference(path.resolve(name), component, app);
            }
        };
    }

    private FileReference createFileReference(Path path, Path component, Path app) {
        return new FileReference() {
            @Override
            public String getName() {
                return path.getFileName().toString();
            }

            @Override
            public String getPathPattern() {
                StringBuilder sb = new StringBuilder();
                for (Path p : component.resolve("pages").relativize(path)) {
                    sb.append('/');
                    sb.append(p.toString());
                }
                return sb.toString();
            }

            @Override
            public String getContent() {
                try {
                    return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new UUFException("Error while reading file " + path, e);
                }
            }

            @Override
            public String getRelativePath() {
                return app.relativize(path).toString();
            }

            @Override
            public Optional<FileReference> getSiblingIfExists(String name) {
                Path sibling = path.resolveSibling(name);
                if (Files.exists(sibling)) {
                    return Optional.of(createFileReference(sibling, component, app));
                } else {
                    return Optional.empty();
                }
            }

        };
    }

    @Override
    public Stream<ComponentReference> resolveComponents(String appName) {
        try {
            Path app = getAppPath(appName).resolve("components");
            return Files.list(app).map(c -> createComponentReference(app, c));
        } catch (IOException e) {
            throw new UUFException("Error while resolving components for " + appName, e);
        }
    }

    private ComponentReference createComponentReference(final Path app, final Path component) {
        return new ComponentReference() {
            @Override
            public Stream<FileReference> streamPageFiles() {
                Path pages = component.resolve("pages");
                if (Files.exists(pages)) {
                    try {
                        return Files.walk(pages).map(path -> createFileReference(path, component, app));
                    } catch (IOException e) {
                        throw new UUFException("Error while finding pages in " + component, e);
                    }
                } else {
                    return Stream.empty();
                }
            }

            @Override
            public Stream<FragmentReference> streamFragmentFiles() {
                try {
                    Path fragments = component.resolve("fragments");
                    if (Files.exists(fragments)) {
                        return Files.list(fragments).map(path -> createFragmentReference(path, component, app));
                    } else {
                        return Stream.empty();
                    }
                } catch (IOException e) {
                    throw new UUFException("Error while listing fragments in " + component, e);
                }
            }

            @Override
            public FileReference resolveLayout(String layoutName) {
                return createFileReference(component.resolve("layouts").resolve(layoutName), component, app);
            }


            @Override
            public String getName() {
                return component.getFileName().toString();
            }
        };
    }

    @Override
    public ComponentReference resolveComponent(String appName, String name) {
        Path appPath = getAppPath(appName);
        Path componentPath = appPath.resolve("components").resolve(name);
        return createComponentReference(appPath, componentPath);
    }
}
