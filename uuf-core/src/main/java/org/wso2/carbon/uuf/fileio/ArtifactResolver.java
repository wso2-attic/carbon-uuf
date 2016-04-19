package org.wso2.carbon.uuf.fileio;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.create.AppReference;
import org.wso2.carbon.uuf.core.create.AppResolver;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ArtifactResolver implements AppResolver {
    private final List<Path> paths;

    /**
     * This constructor will assume uufHome as $PRODUCT_HOME/deployment/uufapps
     */
    public ArtifactResolver() {
        this(Utils.getCarbonHome().resolve("deployment").resolve("uufapps"));
    }

    public ArtifactResolver(Path uufHome) {
        paths = getAllApplications(uufHome);
    }

    private List<Path> getAllApplications(Path root) {
        try {
            return Files.list(root).filter(Files::isDirectory).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UUFException(
                    "Error while reading deployment artifacts on '" + root.toString() + "' folder!");
        }
    }

    @Override
    public AppReference resolve(String name) {
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
