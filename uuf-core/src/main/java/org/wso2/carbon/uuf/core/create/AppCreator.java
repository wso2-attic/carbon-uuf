package org.wso2.carbon.uuf.core.create;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.apache.commons.lang3.tuple.Pair;
import org.wso2.carbon.uuf.core.App;
import org.wso2.carbon.uuf.core.ClassLoaderProvider;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.core.ComponentLookup;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.core.Page;
import org.wso2.carbon.uuf.core.Renderable;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.UriPatten;
import org.wso2.carbon.uuf.core.auth.SessionRegistry;
import org.wso2.carbon.uuf.core.exception.InvalidTypeException;
import org.wso2.carbon.uuf.core.exception.MalformedConfigurationException;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class AppCreator {

    private final Map<String, RenderableCreator> renderableCreators;
    private final Set<String> supportedExtensions;
    private final ClassLoaderProvider classLoaderProvider;

    public AppCreator(Map<String, RenderableCreator> renderableCreators, ClassLoaderProvider classLoaderProvider) {
        this.renderableCreators = renderableCreators;
        this.supportedExtensions = new HashSet<>();
        for (RenderableCreator renderableCreator : renderableCreators.values()) {
            supportedExtensions.addAll(renderableCreator.getSupportedFileExtensions());
        }
        this.classLoaderProvider = classLoaderProvider;
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
            ComponentReference componentReference = appReference.getComponentReference(componentName);
            Component component = createComponent(componentName, componentVersion, componentContext, componentReference,
                                                  componentDependencies);

            componentSiblings.add(component);
            components.add(component);
            previousLevel = currentLevel;
        }
        return new App(context, components, new SessionRegistry());
    }

    private Component createComponent(String componentName, String componentVersion, String componentContext,
                                      ComponentReference componentReference, Set<Component> dependencies) {
        final ClassLoader classLoader = classLoaderProvider.getClassLoader(componentReference);

        Map<String, Fragment> fragments = componentReference
                .getFragments(supportedExtensions)
                .parallel()
                .map((fragmentReference) -> createFragment(fragmentReference, classLoader))
                .collect(Collectors.toMap(Fragment::getName, fragment -> fragment));

        SetMultimap<String, Renderable> bindings;
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
                            "' of component '" + componentReference.getSimpleName() + "' is malformed.", e);
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
                            "' of component '" + componentReference.getSimpleName() + "' is malformed.", e);
        }

        SortedSet<Page> pages = componentReference
                .getPages(supportedExtensions)
                .parallel()
                .map(pageReference -> createPage(pageReference, classLoader, bindings))
                .collect(Collectors.toCollection(TreeSet::new));

        ComponentLookup lookup = new ComponentLookup(componentName, componentContext, new HashSet<>(fragments.values()),
                                                     bindings, dependencies);
        return new Component(componentName, componentVersion, pages, lookup);
    }

    private Fragment createFragment(FragmentReference fragmentReference, ClassLoader classLoader) {
        RenderableCreator renderableCreator = getRenderableCreator(fragmentReference.getRenderingFile());
        Renderable renderer = renderableCreator.createFragmentRenderable(fragmentReference, classLoader);
        return new Fragment(fragmentReference.getName(), renderer);
    }

    private SetMultimap<String, Renderable> createBindings(Map<Object, Object> bindingsConfig,
                                                           Map<String, Fragment> fragments,
                                                           Set<Component> dependencies) {
        SetMultimap<String, Renderable> bindings = HashMultimap.create();
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
                    bindings.put(zoneName, fragment.getRenderer());
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
                        bindings.put(zoneName, fragment.getRenderer());
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

    private Page createPage(PageReference pageReference, ClassLoader classLoader,
                            SetMultimap<String, Renderable> bindings) {
        RenderableCreator renderableCreator = getRenderableCreator(pageReference.getRenderingFile());
        Pair<Renderable, Map<String, ? extends Renderable>> pr = renderableCreator.createPageRenderables(pageReference,
                                                                                                         classLoader);
        Map<String, ? extends Renderable> bindingsFromPage = pr.getValue();
        for (Map.Entry<String, ? extends Renderable> entry : bindingsFromPage.entrySet()) {
            bindings.put(entry.getKey(), entry.getValue());
        }
        UriPatten uriPatten = new UriPatten(pageReference.getPathPattern());
        return new Page(uriPatten, pr.getKey());
    }

    private Pair<String, String> getComponentNameAndVersion(String dependencyLine) {
        int firstColon = dependencyLine.indexOf(':');
        int secondColon = dependencyLine.indexOf(':', firstColon + 1);
        int thirdColon = dependencyLine.indexOf(':', secondColon + 1);
        int forthColon = dependencyLine.indexOf(':', thirdColon + 1);
        return Pair.of(dependencyLine.substring(firstColon + 1, secondColon),
                       dependencyLine.substring(thirdColon + 1, forthColon));
    }

    private String getComponentContext(String componentName) {
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

