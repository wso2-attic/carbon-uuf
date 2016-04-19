package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.create.ComponentReference;
import org.wso2.carbon.uuf.core.create.FileReference;
import org.wso2.carbon.uuf.core.create.FragmentReference;
import org.wso2.carbon.uuf.core.create.PageReference;
import org.wso2.carbon.uuf.core.create.RenderableCreator;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component(
        name = "org.wso2.carbon.uuf.handlebars.HbsRenderableCreator",
        service = RenderableCreator.class,
        immediate = true
)
public class HbsRenderableCreator implements RenderableCreator {

    private static final String EXTENSION_HANDLEBARS = ".hbs";
    private static final String EXTENSION_JAVASCRIPT = ".js";
    private static final Set<String> SUPPORTED_FILE_EXTENSIONS = ImmutableSet.of("hbs");

    @Override
    public Renderable createFragmentRenderable(FragmentReference fragmentReference, ClassLoader classLoader) {
        TemplateSource templateSource = createTemplateSource(fragmentReference.getRenderingFile());
        Optional<Executable> executable = createSameNameJs(fragmentReference.getRenderingFile(), classLoader);
        return new HbsRenderable(templateSource, executable);
    }

    @Override
    public Pair<Renderable, Map<String, ? extends Renderable>> createPageRenderables(PageReference pageReference,
                                                                                     ClassLoader classLoader) {
        TemplateSource templateSource = createTemplateSource(pageReference.getRenderingFile());
        Optional<Executable> executable = createSameNameJs(pageReference.getRenderingFile(), classLoader);
        HbsInitRenderable pageRenderable = new HbsInitRenderable(templateSource, executable);
        Optional<String> layoutFullNameOpt = pageRenderable.getLayoutName();
        Renderable renderable;
        if (layoutFullNameOpt.isPresent()) {
            String layoutFullName = layoutFullNameOpt.get();
            String layoutName;
            int lastDot = layoutFullName.lastIndexOf('.');
            ComponentReference component;
            if (lastDot >= 0) {
                String componentName = layoutFullName.substring(0, lastDot);
                component = pageReference.getAppReference().getComponentReference(componentName);
                layoutName = layoutFullName.substring(lastDot + 1);
            } else {
                component = pageReference.getComponentReference();
                layoutName = layoutFullName;
            }
            FileReference layoutReference = component.resolveLayout(layoutName + EXTENSION_HANDLEBARS);
            renderable = new HbsRenderable(createTemplateSource(layoutReference), pageRenderable.getScript());
        } else {
            renderable = pageRenderable;
        }
        return Pair.of(renderable, pageRenderable.getFillingZones());
    }

    @Override
    public Set<String> getSupportedFileExtensions() {
        return SUPPORTED_FILE_EXTENSIONS;
    }

    private TemplateSource createTemplateSource(FileReference pageReference) {
        return new StringTemplateSource(pageReference.getRelativePath(), pageReference.getContent());
    }

    private Optional<Executable> createSameNameJs(FileReference pageReference, ClassLoader loader) {
        String jsName = withoutExtension(pageReference.getName()) + EXTENSION_JAVASCRIPT;
        Optional<FileReference> jsReference = pageReference.getSibling(jsName);
        return jsReference.map(j -> new JSExecutable(j.getContent(), loader, Optional.of(j.getRelativePath())));
    }

    private String withoutExtension(String name) {
        return name.substring(0, (name.length() - EXTENSION_HANDLEBARS.length()));
    }
}
