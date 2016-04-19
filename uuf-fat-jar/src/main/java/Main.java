import com.google.common.collect.*;
import org.wso2.carbon.uuf.*;
import org.wso2.carbon.uuf.core.*;
import org.wso2.carbon.uuf.core.create.*;
import org.wso2.carbon.uuf.fileio.*;
import org.wso2.carbon.uuf.handlebars.*;
import org.wso2.msf4j.*;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        List<Path> uufAppsPath = Collections.singletonList(FileSystems.getDefault().getPath("."));
        ArtifactResolver resolver = new ArtifactResolver(uufAppsPath);
        ClassLoaderProvider classLoaderCreator = new ClassLoaderProvider() {

            @Override
            public ClassLoader getClassLoader(ComponentReference compReference) {
                return this.getClass().getClassLoader();
            }
        };
        RenderableCreator hbsCreator = new HbsRenderableCreator();
        AppCreator appCreator = new AppCreator(resolver, ImmutableMap.of("hbs", hbsCreator, "js", hbsCreator),
                classLoaderCreator);
        UUFRegistry registry = new UUFRegistry(appCreator, Optional.of(new DebugAppender()), resolver);
        new MicroservicesRunner().deploy(new UUFService(registry)).start();
    }

}
