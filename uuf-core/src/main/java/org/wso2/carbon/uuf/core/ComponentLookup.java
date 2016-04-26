package org.wso2.carbon.uuf.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.uuf.fileio.StaticResolver.DIR_NAME_COMPONENT_RESOURCES;
import static org.wso2.carbon.uuf.fileio.StaticResolver.STATIC_RESOURCE_URI_PREFIX;

public class ComponentLookup {

    private final String componentName;
    private final String fullyQualifiedNamePrefix;
    private final String componentContext;
    private final Map<String, String> dependenciesContexts;
    private final Map<String, Layout> layouts;
    private final SetMultimap<String, Fragment> bindings;
    private final Map<String, Fragment> fragments;

    public ComponentLookup(String componentName, String componentContext, Set<Layout> layouts, Set<Fragment> fragments,
                           SetMultimap<String, Fragment> bindings, Set<Component> dependencies) {
        if (componentName.isEmpty()) {
            throw new IllegalArgumentException("Component name cannot be empty.");
        }
        this.componentName = componentName;
        this.fullyQualifiedNamePrefix = componentName + ".";
        if (!componentContext.startsWith("/")) {
            throw new IllegalArgumentException("Context of a component must start with a '/'.");
        }
        this.componentContext = componentContext;
        this.dependenciesContexts = new HashMap<>();

        this.layouts = layouts.stream().collect(Collectors.toMap(l -> fullyQualifiedName(l.getName()), l -> l));

        this.fragments = fragments.stream().collect(Collectors.toMap(f -> fullyQualifiedName(f.getName()), f -> f));
        this.bindings = HashMultimap.create();
        for (Map.Entry<String, Fragment> entry : bindings.entries()) {
            this.bindings.put(fullyQualifiedName(entry.getKey()), entry.getValue());
        }

        for (Component dependency : dependencies) {
            ComponentLookup dependencyLookup = dependency.getLookup();
            this.dependenciesContexts.put(dependencyLookup.componentName, dependencyLookup.componentContext);
            this.dependenciesContexts.putAll(dependencyLookup.dependenciesContexts);
            this.layouts.putAll(dependencyLookup.layouts);
            this.fragments.putAll(dependencyLookup.fragments);
            this.bindings.putAll(dependencyLookup.bindings);
        }
    }

    private String fullyQualifiedName(String name) {
        return fullyQualifiedNamePrefix + name; // <component-name>.<binding/fragment-name>
    }

    private String getFullyQualifiedName(String name) {
        return (name.indexOf('.') == -1) ? fullyQualifiedName(name) : name;
    }

    public String getComponentName() {
        return componentName;
    }

    String getComponentContext() {
        return componentContext;
    }

    public Optional<Layout> getLayout(String layoutName) {
        return Optional.ofNullable(layouts.get(getFullyQualifiedName(layoutName)));
    }

    public Set<Fragment> getBindings(String zoneName) {
        return bindings.get(getFullyQualifiedName(zoneName));
    }

    public Optional<Fragment> getFragment(String fragmentName) {
        return Optional.ofNullable(fragments.get(getFullyQualifiedName(fragmentName)));
    }

    Map<String, Fragment> getFragments() {
        return fragments;
    }

    String getPublicUriInfix(Page page) {
        return STATIC_RESOURCE_URI_PREFIX + componentContext + "/" + DIR_NAME_COMPONENT_RESOURCES;
    }

    String getPublicUriInfix(Layout layout) {
        int lastDotIndex = layout.getName().lastIndexOf('.');
        if (lastDotIndex == -1) {
            return STATIC_RESOURCE_URI_PREFIX + componentContext + "/" + layout.getName();
        } else {
            String dependencyName = layout.getName().substring(0, lastDotIndex);
            return STATIC_RESOURCE_URI_PREFIX + dependenciesContexts.get(dependencyName);
        }
    }

    String getPublicUriInfix(Fragment fragment) {
        int lastDotIndex = fragment.getName().lastIndexOf('.');
        if (lastDotIndex == -1) {
            return STATIC_RESOURCE_URI_PREFIX + componentContext + "/" + fragment.getName();
        } else {
            String dependencyName = fragment.getName().substring(0, lastDotIndex);
            return STATIC_RESOURCE_URI_PREFIX + dependenciesContexts.get(dependencyName);
        }
    }
}
