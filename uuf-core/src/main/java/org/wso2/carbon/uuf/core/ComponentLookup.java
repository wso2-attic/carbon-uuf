package org.wso2.carbon.uuf.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentLookup {
    private final String componentName;
    private final String componentContext;
    private final SetMultimap<String, Renderable> bindings;
    private final Map<String, Fragment> fragments;

    public ComponentLookup(String componentName, String componentContext, Set<Fragment> fragments,
                           SetMultimap<String, ? extends Renderable> bindings,
                           Set<Component> childComponents) {
        this.componentName = componentName + ".";
        this.componentContext = componentContext;

        this.fragments = fragments.stream().collect(Collectors.toMap(f -> getFullyQualifiedName(f.getName()), f -> f));
        this.bindings = HashMultimap.create();
        for (Map.Entry<String, ? extends Renderable> entry : bindings.entries()) {
            this.bindings.put(getFullyQualifiedName(entry.getKey()), entry.getValue());
        }

        for (Component childComponent : childComponents) {
            ComponentLookup childComponentLookup = childComponent.getComponentLookup();
            this.fragments.putAll(childComponentLookup.fragments);
            this.bindings.putAll(childComponentLookup.bindings);
        }
    }

    private String getFullyQualifiedName(String name) {
        return componentName + name;
    }

    public Optional<Set<? extends Renderable>> getBindings(String zoneName) {
        if (zoneName.indexOf('.') == -1) {
            zoneName = getFullyQualifiedName(zoneName);
        }
        return Optional.ofNullable(bindings.get(zoneName));
    }

    public Optional<Fragment> getFragment(String fragmentName) {
        return Optional.ofNullable(fragments.get(fragmentName));
    }
}
