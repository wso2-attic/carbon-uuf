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

        this.fragments = fragments.stream().collect(Collectors.toMap(f -> fullyQualifiedName(f.getName()), f -> f));
        this.bindings = HashMultimap.create();
        for (Map.Entry<String, ? extends Renderable> entry : bindings.entries()) {
            this.bindings.put(fullyQualifiedName(entry.getKey()), entry.getValue());
        }

        for (Component dependency : dependencies) {
            ComponentLookup dependencyLookup = dependency.getLookup();
            this.dependenciesContexts.put(dependencyLookup.componentName, dependencyLookup.componentContext);
            this.dependenciesContexts.putAll(dependencyLookup.dependenciesContexts);
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

    public String getComponentContext() {
        return componentContext;
    }

    public Set<Renderable> getBindings(String zoneName) {
        return bindings.get(getFullyQualifiedName(zoneName));
    }

    public Optional<Fragment> getFragment(String fragmentName) {
        return Optional.ofNullable(fragments.get(getFullyQualifiedName(fragmentName)));
    }

    Map<String, Fragment> getFragments() {
        return fragments;
    }

    String getPublicUriInfix(Page page) {
        return PUBLIC_URI_CONTEXT + componentContext + "/base";
    }

    String getPublicUriInfix(Fragment fragment) {
        int lastDotIndex = fragment.getName().lastIndexOf('.');
        if (lastDotIndex == -1) {
            return PUBLIC_URI_CONTEXT + componentContext + "/" + fragment.getName();
        } else {
            String dependencyName = fragment.getName().substring(0, lastDotIndex);
            return PUBLIC_URI_CONTEXT + dependenciesContexts.get(dependencyName);
        }
    }
}
