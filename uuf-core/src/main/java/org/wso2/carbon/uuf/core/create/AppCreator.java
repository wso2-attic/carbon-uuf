package org.wso2.carbon.uuf.core.create;

import org.apache.commons.lang3.tuple.Pair;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.BundleCreator;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.UriPatten;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class AppCreator {

    private Resolver resolver;
    private Map<String, RenderableCreator> creators;
    private BundleCreator bundleCreator;

    public AppCreator(
            Resolver resolver,
            Map<String, RenderableCreator> creators,
            BundleCreator bundleCreator) {

        this.resolver = resolver;
        this.creators = creators;
        this.bundleCreator = bundleCreator;
        this.creators = creators;
    }

    public App createApp(String appName, String context) {
        Set<Component> components = new HashSet<>();

        AppReference appReference = resolver.resolveApp(appName);
        List<String> tree = appReference.getDependencyTree();
        int previousLevel = -1;
        Set<Component> siblings = new HashSet<>();
        Set<Component> children;
        LinkedList<Set<Component>> componentLevels = new LinkedList<>();
        for (int i = tree.size() - 1; i >= 0; i--) {
            String line = tree.get(i);
            int level = countLevel(line);
            int jump = level - previousLevel;
            if (previousLevel < 0 || jump == 0) {
                children = Collections.emptySet();
            } else if (jump < 0) {
                children = siblings;
                siblings = componentLevels.poll();
                if (siblings == null) {
                    siblings = new HashSet<>();
                }
            } else { // jump > 0
                children = Collections.emptySet();
                componentLevels.push(siblings);
                siblings = new HashSet<>();
                for (int j = 0; j < jump - 1; j++) {
                    componentLevels.push(new HashSet<>());
                }
            }
            String componentName = extractArtifactId(line);
            String componentContext = getContextFormName(componentName);
            if (i == 0) {
                componentName = "root";
            }
            ComponentReference componentReference = appReference.getComponentReference(componentName);
            Component component = createComponent(componentReference, children);
            siblings.add(component);
            components.add(component);
            previousLevel = level;
        }
        return new App(context, components);
    }

    private String extractArtifactId(String line) {
        int firstColon = line.indexOf(':') + 1;
        int secondColon = line.indexOf(':', firstColon);
        return line.substring(firstColon, secondColon);
    }

    private String getContextFormName(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            return name.substring(lastDot + 1);
        } else {
            return name;
        }
    }

    private int countLevel(String line) {
        int indent = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '+' || c == ' ' || c == '\\' || c == '-') {
                indent++;
            } else {
                break;
            }
        }
        return indent / 3;
    }

    private Optional<Page> createPage(FileReference pageReference, Lookup lookup, ClassLoader loader) {
        String relativePath = pageReference.getPathPattern();
        String extension = pageReference.getExtension();
        RenderableCreator creator = creators.get(extension);
        if (creator == null) {
            throw new UUFException("No creator for '" + extension + "'");
        }
        String path = withoutExtension(relativePath);
        if (path.endsWith("/index")) {
            path = path.substring(0, path.length() - 5);
        }
        UriPatten uriPatten = new UriPatten(path);
        Optional<Pair<Renderable, Map<String, ? extends Renderable>>> o = creator.createRenderableWithBindings(pageReference, loader);
        return o.map(j -> new Page(uriPatten, j.getLeft(), lookup.combine(j.getRight())));
    }

    private Component createComponent(ComponentReference componentReference, Set<Component> children) {
        String name = componentReference.getName();
        String version = componentReference.getVersion();
        String context = componentReference.getContext();

        final ClassLoader classLoader = getClassLoader(componentReference);

        Set<Fragment> fragments = componentReference
                .streamFragmentFiles()
                .parallel()
                .map((fragmentReference) -> createFragment(fragmentReference, classLoader))
                .collect(Collectors.toSet());
        @SuppressWarnings("unchecked")
        Map<String, ?> config = componentReference
                .getConfig()
                .map(b -> (Map<String, ?>) new Yaml().loadAs(b.getContent(), Map.class))
                .orElse(Collections.emptyMap());
        @SuppressWarnings("unchecked")
        Map<String, String> bindigs = componentReference
                .getConfig()
                .map(b -> (Map<String, String>) new Yaml().loadAs(b.getContent(), Map.class))
                .orElse(Collections.emptyMap());
        Lookup lookup = new Lookup(name, Collections.emptyMap(), fragments, children);
        SortedSet<Page> pages = componentReference
                .streamPageFiles()
                .parallel()
                .map(pageReference -> createPage(pageReference, lookup, classLoader))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toCollection(TreeSet::new));

        return new Component(
                name,
                context,
                version,
                pages,
                lookup);
    }

    private Optional<Renderable> crateRenderable(FileReference fileReference, ClassLoader cl) {
        String extension = fileReference.getExtension();
        RenderableCreator creator = creators.get(extension);
        if (creator != null) {
            return creator.createRenderable(fileReference, cl);
        }
        throw new UUFException("No creator for '" + extension + "'");
    }

    private Fragment createFragment(FragmentReference fragmentReference, ClassLoader cl) {
        String component = fragmentReference.getComponentReference().getName();
        String name = component + '.' + fragmentReference.getName();
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

    private ClassLoader getClassLoader(ComponentReference componentReference) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        if (classLoader instanceof BundleReference) {
            //if an OSGi classloader
            Bundle bundle = bundleCreator.createBundleIfNotExists(componentReference);
            classLoader = bundleCreator.getComponentBundleClassLoader(bundle);
        }
        return classLoader;
    }

    private String withoutExtension(String name) {
        int extensionIndex = name.lastIndexOf(".");
        return (extensionIndex == -1) ? name : name.substring(0, extensionIndex);
    }
}

