package org.wso2.carbon.uuf.core.create;

import org.apache.commons.lang3.tuple.Pair;
import org.osgi.framework.BundleReference;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.BundleCreator;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.internal.RenderableCreatorsRepository;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class AppCreator {

    private Resolver resolver;
    private BundleCreator bundleCreator;

    public AppCreator(
            Resolver resolver,
            BundleCreator bundleCreator) {

        this.resolver = resolver;
        this.bundleCreator = bundleCreator;
    }


    public App createApp(String appName, String context) {
        Set<Component> components = resolver
                .resolveApp(appName)
                .streamComponents()
                .map(this::createComponent)
                .collect(Collectors.toSet());
        return new App(context, components);
    }

    private Optional<Page> createPage(FileReference pageReference, ClassLoader loader) {
        String relativePath = pageReference.getPathPattern();
        String extension = pageReference.getExtension();
        RenderableCreator creator = RenderableCreatorsRepository.getInstance().get(extension);
        if (creator != null) {
            String path = withoutExtension(relativePath);
            if (path.endsWith("/index")) {
                path = path.substring(0, path.length() - 5);
            }
            UriPatten uriPatten = new UriPatten(path);
            Optional<Pair<Renderable, Map<String, ? extends Renderable>>> o = creator.createRenderableWithBindings(pageReference, loader);
            return o.map(j -> new Page(uriPatten, j.getLeft(), j.getRight()));
        }
        throw new UUFException("No creator for '" + extension + "'");
    }

    private Component createComponent(ComponentReference componentReference) {
        String name = componentReference.getName();
        String version = componentReference.getVersion();
        String context = componentReference.getContext();

        if (this.getClass().getClassLoader() instanceof BundleReference) {
            //if an OSGi classloader, creates a mapping bundle
            bundleCreator.createBundle(componentReference);
        }
        ClassLoader loader = getBundleComponentClassLoader(componentReference);

        SortedSet<Page> pages = componentReference
                .streamPageFiles()
                .parallel()
                .map(pageReference -> createPage(pageReference, loader))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toCollection(TreeSet::new));
        Set<Fragment> fragments = componentReference
                .streamFragmentFiles()
                .parallel()
                .map((fragmentReference) -> createFragment(fragmentReference, loader))
                .collect(Collectors.toSet());
        @SuppressWarnings("unchecked")
        Map<String, ?> config = componentReference
                .getConfig()
                .map(b -> (Map<String, ?>) new Yaml().loadAs(b.getContent(), Map.class))
                .orElse(Collections.emptyMap());

        return new Component(
                name,
                context,
                version,
                pages,
                fragments,
                config,
                Collections.emptyMap());
    }

    private Optional<Renderable> crateRenderable(FileReference fileReference, ClassLoader cl) {
        String extension = fileReference.getExtension();
        RenderableCreator creator = RenderableCreatorsRepository.getInstance().get(extension);
        if (creator != null) {
            return creator.createRenderable(fileReference, cl);
        }
        throw new UUFException("No creator for '" + extension + "'");
    }

    private Fragment createFragment(FragmentReference fragmentReference, ClassLoader cl) {
        String name = fragmentReference.getName();
        Renderable renderable = fragmentReference
                .streamChildren()
                .map(f -> crateRenderable(f, cl))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() ->
                        new UUFException("Fragment has not renderable file " + fragmentReference));
        return new Fragment(name, renderable);
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

    private String withoutExtension(String name) {
        //TODO: fix for short ext
        return name.substring(0, name.length() - 4);
    }
}

