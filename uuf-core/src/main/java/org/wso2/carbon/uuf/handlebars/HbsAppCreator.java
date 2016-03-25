package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import org.osgi.framework.BundleReference;
import org.wso2.carbon.uuf.core.*;
import org.wso2.carbon.uuf.core.create.AppCreator;
import org.wso2.carbon.uuf.core.create.ComponentReference;
import org.wso2.carbon.uuf.core.create.FileReference;
import org.wso2.carbon.uuf.core.create.FragmentReference;

import java.util.*;
import java.util.stream.Collectors;

public class HbsAppCreator implements AppCreator {

    private Resolver resolver;
    private BundleCreator bundleCreator;
    public HbsAppCreator(Resolver resolver, BundleCreator bundleCreator) {
        this.resolver = resolver;
        this.bundleCreator = bundleCreator;
    }


    @Override
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

        String relativePath = pageReference.getPathPattern();
        String path = withoutHbsExtension(relativePath);
        if (path.endsWith("/index")) {
            path = path.substring(0, path.length() - 5);
        }
        UriPatten uriPatten = new UriPatten(path);
        TemplateSource templateSource = createTemplateSource(pageReference);
        Optional<Executable> executable = createSameNameJs(pageReference, currentComponent);
        HbsInitRenderable pageRenderable = new HbsInitRenderable(templateSource, executable);
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

    private Optional<Executable> createSameNameJs(FileReference pageReference, ComponentReference currentComponent) {
        String jsName = withoutHbsExtension(pageReference.getName()) + ".js";
        Optional<FileReference> jsReference = pageReference.getSiblingIfExists(jsName);
        //TODO: need to get actual component here not the "root"
        return jsReference.map(j ->
                new JSExecutable(j.getContent(), Optional.of(j.getRelativePath()), getBundleComponentClassLoader(currentComponent)));
    }

    private Component createComponent(ComponentReference componentReference, String appName) {
        String name = componentReference.getName();
        String version = componentReference.getVersion();
        String context = componentReference.getContext();

        if (this.getClass().getClassLoader() instanceof BundleReference) {
            //if an OSGi classloader, creates a mapping bundle
            bundleCreator.createBundle(componentReference);
        }

        SortedSet<Page> pages = componentReference
                .streamPageFiles()
                .parallel()
                .filter(p -> p.getName().endsWith(".hbs"))
                .map(fileReference -> createPage(fileReference, componentReference, appName))
                .collect(Collectors.toCollection(TreeSet::new));
        Set<Fragment> fragments = componentReference
                .streamFragmentFiles()
                .parallel()
                .map(f->createFragment(f,componentReference))
                .collect(Collectors.toSet());

        return new Component(
                name,
                context,
                version,
                pages,
                fragments,
                Collections.emptyMap(),
                Collections.emptyMap());
    }

    private Fragment createFragment(FragmentReference dir, ComponentReference currentComponent) {
        String name = dir.getName();
        FileReference hbsFile = dir.getChild(name + ".hbs");
        TemplateSource templateSource = createTemplateSource(hbsFile);
        Optional<Executable> executable = createSameNameJs(hbsFile, currentComponent);
        return new Fragment(name, new HbsRenderable(templateSource, executable));
    }


    private TemplateSource createTemplateSource(FileReference pageReference) {
        return new StringTemplateSource(
                pageReference.getRelativePath(),
                pageReference.getContent());
    }

    private ClassLoader getBundleComponentClassLoader(ComponentReference compReference) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        if (classLoader instanceof BundleReference) {
            //if an OSGi classloader
            String bundleLocKey = bundleCreator.getBundleLocationKey(compReference.getName(), compReference.getContext());
            classLoader = bundleCreator.getBundleClassLoader(bundleLocKey);
        }
        return classLoader;
    }

    private String withoutHbsExtension(String name) {
        return name.substring(0, name.length() - 4);
    }
}
