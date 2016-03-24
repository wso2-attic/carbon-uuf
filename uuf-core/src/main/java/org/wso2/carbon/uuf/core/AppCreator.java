package org.wso2.carbon.uuf.core;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.handlebars.Executable;
import org.wso2.carbon.uuf.handlebars.HbsInitRenderable;
import org.wso2.carbon.uuf.handlebars.HbsRenderable;
import org.wso2.carbon.uuf.handlebars.JSExecutable;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class AppCreator {

    private Resolver resolver;

    public AppCreator(Resolver resolver) {
        this.resolver = resolver;
    }


    public App createApp(String appName, String context) {
        Set<Component> components = resolver.resolveComponents(appName)
                .map((componentReference) -> createComponent(componentReference, appName))
                .collect(Collectors.toSet());
        return new App(context, components);
    }

    private Page createPage(
            FileReference pageReference,
            ComponentReference currentComponent,
            String appName) {

        String relativePath = pageReference.getPathRelativeToPagesRoot();
        String path = withoutHbsExtension("/" + relativePath);
        //TODO: handle windows
        if (path.endsWith("/index")) {
            path = path.substring(0, path.length() - 5);
        }
        UriPatten uriPatten = new UriPatten(path);
        HbsInitRenderable pageRenderable = createHbsInitRenderable(pageReference);
        Optional<String> layoutFullName = pageRenderable.getLayoutName();
        Renderable renderable = layoutFullName
                .map(fullName -> {
                    String layoutName;
                    int lastDot = fullName.lastIndexOf('.');
                    ComponentReference component;
                    if (lastDot >= 0) {
                        String componentName = fullName.substring(0, lastDot);
                        component = resolver.resolveComponent(appName, componentName);
                        layoutName = fullName.substring(lastDot + 1);
                    } else {
                        component = currentComponent;
                        layoutName = fullName;
                    }
                    FileReference layoutReference = component.resolveLayout(layoutName + ".hbs");
                    return new HbsRenderable(
                            createTemplateSource(layoutReference),
                            pageRenderable.getScript());
                })
                .orElse(pageRenderable);
        return new Page(uriPatten, renderable, pageRenderable.getFillingZones());
    }

    private HbsInitRenderable createHbsInitRenderable(FileReference pageReference) {
        TemplateSource templateSource = createTemplateSource(pageReference);
        String jsName = withoutHbsExtension(pageReference.getName()) + ".js";
        Optional<FileReference> jsReference = pageReference.getSibling(jsName);
        Optional<Executable> executable = jsReference.map(j ->
                new JSExecutable(j.getContent(), Optional.of(j.getPathRelativeToApp())));
        return new HbsInitRenderable(
                templateSource,
                executable);
    }

    private Component createComponent(ComponentReference componentReference, String appName) {
        SortedSet<Page> pages = componentReference
                .streamPageFiles()
                .parallel()
                .filter(p -> p.getName().endsWith(".hbs"))
                .map(fileReference -> createPage(fileReference, componentReference, appName))
                .collect(Collectors.toCollection(TreeSet::new));
        Set<Fragment> fragments = componentReference
                .streamFragmentFiles()
                .parallel()
                .map(this::createFragment)
                .collect(Collectors.toSet());

        String name = componentReference.getName();
        return new Component(
                name,
                "/" + (name.equals("root") ? "" : getContextFormName(name)),
                pages,
                fragments,
                Collections.emptyMap(),
                Collections.emptyMap());
    }

    private Fragment createFragment(FileReference dir) {
        String name = dir.getName();
        return new Fragment(name, createHbsInitRenderable(dir.getChild(name + ".hbs")));
    }


    private TemplateSource createTemplateSource(FileReference pageReference) {
        return new StringTemplateSource(
                pageReference.getPathRelativeToApp(),
                pageReference.getContent());
    }

    private String getContextFormName(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            return name.substring(lastDot + 1);
        } else {
            return name;
        }
    }

    private String withoutHbsExtension(String name) {
        return name.substring(0, name.length() - 4);
    }
}
