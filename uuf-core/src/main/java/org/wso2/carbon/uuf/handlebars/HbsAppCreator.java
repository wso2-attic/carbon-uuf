package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import org.osgi.framework.BundleReference;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.BundleCreator;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.core.create.AppCreator;
import org.wso2.carbon.uuf.core.create.ComponentReference;
import org.wso2.carbon.uuf.core.create.FileReference;
import org.wso2.carbon.uuf.core.create.FragmentReference;
import org.wso2.carbon.uuf.core.create.Resolver;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
        Set<Component> components = resolver
                .resolveApp(appName)
                .streamComponents()
                .map(this::createComponent)
                .collect(Collectors.toSet());
        return new App(context, components);
    }

    private Page createPage(FileReference pageReference) {
        String relativePath = pageReference.getPathPattern();
        String path = withoutHbsExtension(relativePath);
        if (path.endsWith("/index")) {
            path = path.substring(0, path.length() - 5);
        }
        UriPatten uriPatten = new UriPatten(path);
        TemplateSource templateSource = createTemplateSource(pageReference);
        Optional<Executable> executable = createSameNameJs(pageReference);
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
        return new Page(uriPatten, renderable, pageRenderable.getFillingZones());
    }

    private Optional<Executable> createSameNameJs(FileReference pageReference) {
        String jsName = withoutHbsExtension(pageReference.getName()) + ".js";
        Optional<FileReference> jsReference = pageReference.getSiblingIfExists(jsName);
        //TODO: need to get actual component here not the "root"
        return jsReference.map(j ->
                new JSExecutable(j.getContent(), getBundleComponentClassLoader(pageReference.getComponentReference()), Optional.of(j.getRelativePath())));
    }

    private Component createComponent(ComponentReference componentReference) {
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
                .map(this::createPage)
                .collect(Collectors.toCollection(TreeSet::new));
        Set<Fragment> fragments = componentReference
                .streamFragmentFiles()
                .parallel()
                .map(this::createFragment)
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

    private Fragment createFragment(FragmentReference fragmentReference) {
        String name = fragmentReference.getName();
        FileReference hbsFile = fragmentReference.getChild(name + ".hbs");
        TemplateSource templateSource = createTemplateSource(hbsFile);
        Optional<Executable> executable = createSameNameJs(hbsFile);
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
            String bundleLocKey = bundleCreator
                    .getBundleLocationKey(compReference.getName(), compReference.getContext());
            classLoader = bundleCreator.getBundleClassLoader(bundleLocKey);
        }
        return classLoader;
    }

    private String withoutHbsExtension(String name) {
        return name.substring(0, name.length() - 4);
    }
}
