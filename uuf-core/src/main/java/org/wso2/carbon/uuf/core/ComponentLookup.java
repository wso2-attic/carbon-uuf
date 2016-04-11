package org.wso2.carbon.uuf.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentLookup {
    private static final String PUBLIC_URI_CONTEXT = "/public";

    private final String componentName;
    private final String fullyQualifiedNamePrefix;
    private final String componentContext;
    private final Map<String, String> dependenciesContexts;
    private final SetMultimap<String, Renderable> bindings;
    private final Map<String, Fragment> fragments;

    public ComponentLookup(String componentName, String componentContext, Set<Fragment> fragments,
                           SetMultimap<String, ? extends Renderable> bindings, Set<Component> dependencies) {
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

        this.fragments = fragments.stream().collect(Collectors.toMap(f -> getFullyQualifiedName(f.getName()), f -> f));
        this.bindings = HashMultimap.create();
        for (Map.Entry<String, ? extends Renderable> entry : bindings.entries()) {
            this.bindings.put(getFullyQualifiedName(entry.getKey()), entry.getValue());
        }

        for (Component dependency : dependencies) {
            ComponentLookup dependencyLookup = dependency.getLookup();
            this.dependenciesContexts.put(dependencyLookup.componentName, dependencyLookup.componentContext);
            this.dependenciesContexts.putAll(dependencyLookup.dependenciesContexts);
            this.fragments.putAll(dependencyLookup.fragments);
            this.bindings.putAll(dependencyLookup.bindings);
        }
    }

    private String getFullyQualifiedName(String name) {
        return fullyQualifiedNamePrefix + name; // <component-name>.<binding/fragment-name>
    }

    private String computeFullyQualifiedName(String name) {
        return (name.indexOf('.') == -1) ? getFullyQualifiedName(name) : name;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentContext() {
        return componentContext;
    }

    public Optional<Set<? extends Renderable>> getBindings(String zoneName) {
        return Optional.ofNullable(bindings.get(computeFullyQualifiedName(zoneName)));
    }

    public Optional<Fragment> getFragment(String fragmentName) {
        return Optional.ofNullable(fragments.get(computeFullyQualifiedName(fragmentName)));
    }

    public String getPublicUriInfix() {
        return PUBLIC_URI_CONTEXT + componentContext + "/base";
    }

    public String getPublicUriInfixOf(String fragmentName) {
        int lastDotIndex = fragmentName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return PUBLIC_URI_CONTEXT + componentContext + "/" + fragmentName;
        } else {
            String dependencyName = fragmentName.substring(0, lastDotIndex);
            return PUBLIC_URI_CONTEXT + dependenciesContexts.get(dependencyName);
        }
    }
}
