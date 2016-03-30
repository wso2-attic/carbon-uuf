package org.wso2.carbon.uuf.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Lookup {
    private final Map<String, Renderable> bindings;
    private final Map<String, Fragment> fragments;
    private final String componentName;

    private Lookup(String componentName, Map<String, Renderable> bindings, Map<String, Fragment> fragments) {
        this.bindings = bindings;
        this.fragments = fragments;
        this.componentName = componentName;
    }

    public Lookup(
            String componentName,
            Map<String, String> bindings,
            Set<Fragment> localFragments,
            Set<Component> children) {

        this.componentName = componentName;
        this.fragments = localFragments
                .stream()
                .collect(Collectors.toMap(f -> validate(f.getName()), Function.identity()));
        for (Component child : children) {
            Lookup lookup = child.getLookup();
            this.fragments.putAll(lookup.fragments);
        }
        this.bindings = bindings
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        p -> findRenderer(p.getValue(), componentName, this.fragments)));
        for (Component child : children) {
            Lookup lookup = child.getLookup();
            this.fragments.putAll(lookup.fragments);
        }
    }

    private String validate(String name) {
        if (name.startsWith(componentName) && name.indexOf('.', componentName.length() + 1) < 0) {
            return name;
        } else {
            throw new UUFException(
                    "Foreign fragment " + name + " can't be registered under " + componentName);
        }
    }

    private Renderable findRenderer(
            String name,
            String componentName,
            Map<String, Fragment> childFragments) {
        String qualifiedName = qualify(name);
        Fragment fragment = childFragments.get(qualifiedName);
        return fragment.getRenderer();
    }

    private String qualify(String name) {
        return name.indexOf('.') >= 0 ? name : componentName + '.' + name;
    }

    public Lookup combine(Map<String, ? extends Renderable> pageLookup) {
        Map<String, Renderable> bindings = pageLookup
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        en -> qualify(en.getKey()),
                        Map.Entry::getValue));
        bindings.putAll(this.bindings);
        return new Lookup(this.componentName, bindings, this.fragments);
    }

    public Collection<Renderable> lookupBinding(String zoneName) {
        Renderable renderable = bindings.get(qualify(zoneName));
        if (renderable != null) {
            return Collections.singleton(renderable);
        }

        throw new UUFException("Zone '" + zoneName + "' does not have a binding.");
    }

    public Fragment lookupFragment(String fragmentName) {
        Fragment fragment = fragments.get(qualify(fragmentName));
        if (fragment != null) {
            return fragment;
        } else {
            throw new UUFException("Fragment '" + fragmentName + "' does not exists.");
        }
    }
}
