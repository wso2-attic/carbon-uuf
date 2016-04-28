package org.wso2.carbon.uuf.core.create;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.apache.commons.lang3.tuple.Pair;
import org.osgi.service.jndi.JNDIContextManager;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.ClassLoaderProvider;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Layout;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.core.auth.SessionRegistry;
import org.wso2.carbon.uuf.core.exception.InvalidTypeException;
import org.wso2.carbon.uuf.core.exception.MalformedConfigurationException;
import org.wso2.carbon.uuf.core.exception.UUFException;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
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

    private final Map<String, RenderableCreator> renderableCreators;
    private final Set<String> supportedExtensions;
    private final ClassLoaderProvider classLoaderProvider;

    public AppCreator(Set<RenderableCreator> renderableCreators, ClassLoaderProvider classLoaderProvider) {
        this.renderableCreators = new HashMap<>();
        this.supportedExtensions = new HashSet<>();
        for (RenderableCreator renderableCreator : renderableCreators) {
            for (String extension : renderableCreator.getSupportedFileExtensions()) {
                this.renderableCreators.put(extension, renderableCreator);
            }
            supportedExtensions.addAll(renderableCreator.getSupportedFileExtensions());
        }
        this.classLoaderProvider = classLoaderProvider;
    }

    private int countLevel(String line) {
        int indent = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '+' || c == ' ' || c == '\\' || c == '|') {
                indent++;
            } else {
                break;
            }
        }
        return indent/2;
    }

    public App createApp(String context, AppReference appReference) {
        List<String> dependencyLines = appReference.getDependencies();
        int previousLevel = -1;
        Set<Component> componentSiblings = new HashSet<>();
        Set<Component> componentDependencies;
        Deque<Set<Component>> componentLevels = new LinkedList<>();
        Set<Component> components = new HashSet<>();

        for (int i = dependencyLines.size() - 1; i >= 0; i--) {
            String line = dependencyLines.get(i);
            int currentLevel = countLevel(line);
            int jump = currentLevel - previousLevel;
            if ((jump == 0) || (previousLevel < 0)) {
                componentDependencies = Collections.<Component>emptySet();
            } else if (jump < 0) {
                componentDependencies = componentSiblings;
                componentSiblings = componentLevels.poll();
                if (componentSiblings == null) {
                    componentSiblings = new HashSet<>();
                }
            } else { // jump > 0
                componentDependencies = Collections.<Component>emptySet();
                componentLevels.push(componentSiblings);
                componentSiblings = new HashSet<>();
                for (int j = 0; j < jump - 1; j++) {
                    componentLevels.push(new HashSet<>());
                }
            }

            String componentName, componentVersion, componentContext;
            if (i == 0) {
                componentName = Component.ROOT_COMPONENT_NAME;
                componentVersion = getComponentNameAndVersion(line).getRight();
                componentContext = Component.ROOT_COMPONENT_CONTEXT;
            } else {
                Pair<String, String> componentNameAndVersion = getComponentNameAndVersion(line);
                componentName = componentNameAndVersion.getLeft();
                componentVersion = componentNameAndVersion.getRight();
                componentContext = getComponentContext(componentName);
            }
            ComponentReference componentReference = appReference.getComponentReference(getSimpleName(componentName));
            ClassLoader componentClassLoader = classLoaderProvider.getClassLoader(appReference.getName(), componentName,
                                                                                  componentVersion, componentReference);
            Component component = createComponent(componentName, componentVersion, componentContext, componentReference,
                                                  componentDependencies, componentClassLoader);

            componentSiblings.add(component);
            components.add(component);
            previousLevel = currentLevel;
        }
        return new App(context, components, new SessionRegistry(appReference.getName()));
    }

    private Component createComponent(String componentName, String componentVersion, String componentContext,
                                      ComponentReference componentReference, Set<Component> dependencies,
                                      ClassLoader classLoader) {

        Set<Layout> layouts = componentReference
                .getLayouts(supportedExtensions)
                .parallel()
                .map(this::createLayout)
                .collect(Collectors.toSet());

        Map<String, Fragment> fragments = componentReference
                .getFragments(supportedExtensions)
                .parallel()
                .map((fragmentReference) -> createFragment(fragmentReference, classLoader))
                .collect(Collectors.toMap(Fragment::getName, fragment -> fragment));

        SetMultimap<String, Fragment> bindings;
        try {
            @SuppressWarnings("unchecked")
            Map<Object, Object> bindingsConfig = componentReference
                    .getBindingsConfig()
                    .map(fileReference -> new Yaml().loadAs(fileReference.getContent(), Map.class))
                    .orElse(Collections.emptyMap());
            bindings = createBindings(bindingsConfig, fragments, dependencies);
        } catch (Exception e) {
            // Yaml.loadAs() throws an Exception
            throw new MalformedConfigurationException(
                    "Bindings configuration '" + componentReference.getBindingsConfig().get().getRelativePath() +
                            "' of component '" + getSimpleName(componentName) + "' is malformed.", e);
        }

        Map<String, ?> configurations;
        try {
            @SuppressWarnings("unchecked")
            Map rawConfigurations = componentReference
                    .getConfigurations()
                    .map(fileReference -> new Yaml().loadAs(fileReference.getContent(), Map.class))
                    .orElse(Collections.emptyMap());
            // TODO: make sure that this configurations object is a Map<String, Object>
            configurations = rawConfigurations;
        } catch (Exception e) {
            // Yaml.loadAs() throws an Exception
            throw new MalformedConfigurationException(
                    "Configuration '" + componentReference.getConfigurations().get().getRelativePath() +
                            "' of component '" + getSimpleName(componentName) + "' is malformed.", e);
        }

        ComponentLookup lookup = new ComponentLookup(componentName, componentContext, layouts,
                                                     new HashSet<>(fragments.values()), bindings, dependencies);
        SortedSet<Page> pages = componentReference
                .getPages(supportedExtensions)
                .parallel()
                .map(pageReference -> createPage(pageReference, classLoader, lookup))
                .collect(Collectors.toCollection(TreeSet::new));

        return new Component(componentName, componentVersion, pages, lookup);
    }

    private Layout createLayout(LayoutReference layoutReference) {
        RenderableCreator renderableCreator = getRenderableCreator(layoutReference.getRenderingFile());
        Renderable renderer = renderableCreator.createLayoutRenderable(layoutReference);
        return new Layout(layoutReference.getName(), renderer);
    }

    private Fragment createFragment(FragmentReference fragmentReference, ClassLoader classLoader) {
        RenderableCreator renderableCreator = getRenderableCreator(fragmentReference.getRenderingFile());
        Renderable renderer = renderableCreator.createFragmentRenderable(fragmentReference, classLoader);
        return new Fragment(fragmentReference.getName(), renderer);
    }

    private SetMultimap<String, Fragment> createBindings(Map<Object, Object> bindingsConfig,
                                                         Map<String, Fragment> fragments, Set<Component> dependencies) {
        SetMultimap<String, Fragment> bindings = HashMultimap.create();
        if (bindingsConfig.isEmpty()) {
            return bindings;
        }

        Map<String, Fragment> allFragments = new HashMap<>(fragments);
        for (Component dependency : dependencies) {
            allFragments.putAll(dependency.getFragments());
        }

        for (Map.Entry<Object, Object> entry : bindingsConfig.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new InvalidTypeException(
                        "A key (zone name) in the bindings configuration must be a string. Instead found '" +
                                entry.getKey().getClass() + "'.");
            }
            String zoneName = (String) entry.getKey();
            if (entry.getValue() instanceof String) {
                String fragmentName = (String) entry.getValue();
                Fragment fragment = allFragments.get(fragmentName);
                if (fragment == null) {
                    throw new IllegalArgumentException(
                            "Fragment '" + fragmentName + "' does not exists in this component or its dependencies.");
                } else {
                    bindings.put(zoneName, fragment);
                }
            } else if (entry.getValue() instanceof ArrayList) {
                ArrayList fragmentsNames = (ArrayList) entry.getValue();
                for (Object fragmentName : fragmentsNames) {
                    if (!(fragmentName instanceof String)) {
                        throw new InvalidTypeException("An array of values (fragment names) in the bindings " +
                                                               "configuration must be a string array. Instead found '" +
                                                               fragmentName.getClass() + "'.");
                    }
                    Fragment fragment = allFragments.get(fragmentName);
                    if (fragment == null) {
                        throw new IllegalArgumentException("Fragment '" + fragmentName + "' does not exists in this " +
                                                                   "component or its dependencies.");
                    } else {
                        bindings.put(zoneName, fragment);
                    }
                }
            } else {
                throw new InvalidTypeException("A value (fragment name/s) in the bindings configuration must be " +
                                                       "either a string or a string array. Instead found '" +
                                                       entry.getValue().getClass() + "'");
            }
        }

        return bindings;
    }

    private Page createPage(PageReference pageReference, ClassLoader classLoader, ComponentLookup lookup) {
        RenderableCreator renderableCreator = getRenderableCreator(pageReference.getRenderingFile());
        Pair<Renderable, Optional<String>> pr = renderableCreator.createPageRenderable(pageReference, classLoader);
        UriPatten uriPatten = new UriPatten(pageReference.getPathPattern());
        if (pr.getRight().isPresent()) {
            // This page has a layout.
            String layoutName = pr.getRight().get();
            Optional<Layout> layout = lookup.getLayout(layoutName);
            if (layout.isPresent()) {
                return new Page(uriPatten, pr.getLeft(), layout.get());
            } else {
                throw new IllegalArgumentException("Layout '" + layoutName + "' mentioned in page '" +
                                                           pageReference.getRenderingFile().getRelativePath() +
                                                           "' does not exists in component '" +
                                                           lookup.getComponentName() + "' or its dependencies.");
            }
        } else {
            // This page does not have a layout.
            return new Page(uriPatten, pr.getLeft());
        }
    }

    private Pair<String, String> getComponentNameAndVersion(String dependencyLine) {
        // FORMAT: <group ID>:<artifact ID>:<artifact type>:<artifact version><":compile" or end of line>
        String[] parts = dependencyLine.split(":");
        if ((parts.length != 4) && (parts.length != 5)) {
            throw new MalformedConfigurationException(
                    "Dependency line '" + dependencyLine + "' is incorrect. It must be in '" +
                            "<group ID>:<artifact ID>:<artifact type>:<artifact version><\":compile\" or end of line>" +
                            "' format.");
        }
        // component name = <artifact ID> (2nd part), component version = <artifact version> (4th part)
        return Pair.of(parts[1], parts[3]);
    }

    private String getComponentContext(String componentName) {
        return "/" + getSimpleName(componentName);
    }

    private String getSimpleName(String componentName) {
        int lastDot = componentName.lastIndexOf('.');
        if (lastDot >= 0) {
            return componentName.substring(lastDot + 1);
        } else {
            return componentName;
        }
    }

    private RenderableCreator getRenderableCreator(FileReference fileReference) {
        RenderableCreator renderableCreator = renderableCreators.get(fileReference.getExtension());
        if (renderableCreator == null) {
            throw new UUFException(
                    "Cannot find a RenderableCreator for file type '" + fileReference.getExtension() + "'.");
        }
        return renderableCreator;
    }
}
