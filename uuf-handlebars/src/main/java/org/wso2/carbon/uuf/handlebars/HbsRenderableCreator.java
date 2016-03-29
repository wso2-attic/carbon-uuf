package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.create.ComponentReference;
import org.wso2.carbon.uuf.core.create.FileReference;
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

    @Override
    public Optional<Renderable> createRenderable(FileReference fileReference, ClassLoader cl) {
        if (fileReference.getExtension().equals("js")) {
            //JS alone can't be rendered
            return Optional.empty();
        }

        TemplateSource templateSource = createTemplateSource(fileReference);
        Optional<Executable> executable = createSameNameJs(fileReference, cl);
        return Optional.of(new HbsRenderable(templateSource, executable));
    }

    @Override
    public Optional<Pair<Renderable, Map<String, ? extends Renderable>>> createRenderableWithBindings
            (FileReference pageReference, ClassLoader loader) {
        if (pageReference.getExtension().equals("js")) {
            //JS alone can't be rendered
            return Optional.empty();
        }

        TemplateSource templateSource = createTemplateSource(pageReference);
        Optional<Executable> executable = createSameNameJs(pageReference, loader);
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
                component = pageReference.getAppReference()
                        .getComponentReference(componentName);
                layoutName = layoutFullName.substring(lastDot + 1);
            } else {
                component = pageReference.getComponentReference();
                layoutName = layoutFullName;
            }
            FileReference layoutReference = component.resolveLayout(layoutName + ".hbs");
            renderable = new HbsRenderable(
                    createTemplateSource(layoutReference),
                    pageRenderable.getScript());
        } else {
            renderable = pageRenderable;
        }
        return Optional.of(Pair.of(renderable, pageRenderable.getFillingZones()));
    }

    @Override
    public Set<String> getSupportedFileExtensions() {
        return ImmutableSet.of("js", "hbs");
    }

    private TemplateSource createTemplateSource(FileReference pageReference) {
        return new StringTemplateSource(
                pageReference.getRelativePath(),
                pageReference.getContent());
    }

    private Optional<Executable> createSameNameJs(FileReference pageReference, ClassLoader loader) {
        String jsName = withoutExtension(pageReference.getName()) + ".js";
        Optional<FileReference> jsReference = pageReference.getSiblingIfExists(jsName);
        return jsReference.map(j ->
                new JSExecutable(j.getContent(), loader, Optional.of(j.getRelativePath())));
    }


    private String withoutExtension(String name) {
        //TODO: fix for short ext
        return name.substring(0, name.length() - 4);
    }
}
