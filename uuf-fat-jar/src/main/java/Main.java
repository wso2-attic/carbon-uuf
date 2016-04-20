import com.google.common.collect.ImmutableSet;
import org.wso2.carbon.uuf.DebugAppender;
import org.wso2.carbon.uuf.UUFRegistry;
import org.wso2.carbon.uuf.UUFService;
import org.wso2.carbon.uuf.core.ClassLoaderProvider;
import org.wso2.carbon.uuf.core.create.AppCreator;
import org.wso2.carbon.uuf.core.create.RenderableCreator;
import org.wso2.carbon.uuf.fileio.ArtifactResolver;
import org.wso2.carbon.uuf.fileio.StaticResolver;
import org.wso2.carbon.uuf.handlebars.HbsRenderableCreator;
import org.wso2.msf4j.MicroservicesRunner;

import java.nio.file.FileSystems;
import java.util.Optional;

public class Main {

    public static void main(String[] args)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {

        ArtifactResolver appResolver = new ArtifactResolver(FileSystems.getDefault().getPath("."));
        StaticResolver staticResolver = new StaticResolver(FileSystems.getDefault().getPath("."));
        ClassLoaderProvider classLoaderProvider = (an, cn, cv, cr) -> Main.class.getClassLoader();
        RenderableCreator hbsCreator = new HbsRenderableCreator();
        AppCreator appCreator = new AppCreator(ImmutableSet.of(hbsCreator), classLoaderProvider);
        UUFRegistry registry = new UUFRegistry(appCreator, Optional.of(new DebugAppender()), appResolver,
                                               staticResolver);
        new MicroservicesRunner().deploy(new UUFService(registry)).start();
    }
}
