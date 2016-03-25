package org.wso2.carbon.uuf.fileio;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.create.AppReference;
import org.wso2.carbon.uuf.core.create.Resolver;

import javax.ws.rs.core.Response;
import java.nio.file.Path;
import java.util.List;

public class ArtifactResolver implements Resolver {
    private final List<Path> paths;

    public ArtifactResolver(List<Path> paths) {
        this.paths = paths;
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
     * @param appName application name
     * @param resourceUri resource uri
     * @return resolved path
     */
    @Override
    public Path resolveStatic(String appName, String resourceUri) {
        Path appPath = resolveArtifactApp(appName).getPath();
        String resourcePathParts[] = resourceUri.split("/");

        if (resourcePathParts.length < 5) {
            throw new IllegalArgumentException("Invalid resourceUri! `" + resourceUri + "`");
        }

        String resourceUriPrefixPart = resourcePathParts[1];
        String componentUriPart = resourcePathParts[2];
        String fragmentUriPart = resourcePathParts[3];
        int fourthSlash = StringUtils.ordinalIndexOf(resourceUri, "/", 4);
        String subResourcePath = resourceUri.substring(fourthSlash + 1, resourceUri.length());

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

    @Override
    public AppReference resolveApp(String name) {
        return resolveArtifactApp(name);
    }


    private ArtifactAppReference resolveArtifactApp(String name) {
        // app list mush be <white-space> and comma separated. <white-space> in app names not allowed
        for (Path uufAppPath : paths) {
            Path path = uufAppPath.toAbsolutePath().normalize();
            if (name.equals(path.getFileName().toString())) {
                return new ArtifactAppReference(path);
            }
        }
        throw new UUFException("app by the name '" + name + "' is not found!",
                Response.Status.NOT_FOUND);

    }

}
