import com.google.common.collect.ImmutableMap;
import org.wso2.carbon.uuf.UUFRegistry;
import org.wso2.carbon.uuf.UUFService;
import org.wso2.carbon.uuf.core.BundleCreator;
import org.wso2.carbon.uuf.core.create.AppCreator;
import org.wso2.carbon.uuf.core.create.RenderableCreator;
import org.wso2.carbon.uuf.fileio.ArtifactResolver;
import org.wso2.carbon.uuf.fileio.InMemoryBundleCreator;
import org.wso2.carbon.uuf.handlebars.HbsRenderableCreator;
import org.wso2.msf4j.MicroservicesRunner;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        List<Path> uufAppsPath = Collections.singletonList(FileSystems.getDefault().getPath("."));
        ArtifactResolver resolver = new ArtifactResolver(uufAppsPath);
        BundleCreator bundleCreator = new InMemoryBundleCreator();
        RenderableCreator hbsCreator = new HbsRenderableCreator();
        AppCreator appCreator = new AppCreator(resolver, ImmutableMap.of("hbs", hbsCreator, "js", hbsCreator), bundleCreator);
        UUFRegistry registry = new UUFRegistry(appCreator, Optional.empty(), resolver);
        new MicroservicesRunner().deploy(new UUFService(registry)).start();
    }

}
