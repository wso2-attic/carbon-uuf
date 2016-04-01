package org.wso2.carbon.uuf.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Lookup {
    private final Map<String, Fragment> bindings;
    private final Map<String, Fragment> fragments;
    private final String componentName;
    private final String context;

    private Lookup(String componentName, String context, Map<String, Fragment> bindings, Map<String, Fragment> fragments) {
        this.bindings = bindings;
        this.fragments = fragments;
        this.componentName = componentName;
        this.context = context;
    }

    public Lookup(
            String componentName,
            String context,
            Map<String, String> bindings,
            Set<Fragment> localFragments,
            Set<Component> children) {

        this.componentName = componentName;
        this.context = context;
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
                        p -> this.fragments.get(qualify(p.getValue()))));
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

    private String qualify(String name) {
        return name.indexOf('.') >= 0 ? name : componentName + '.' + name;
    }

    public Lookup combine(Map<String, ? extends Renderable> pageLookup) {
        Map<String, Fragment> bindings = pageLookup
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        en -> qualify(en.getKey()),
                        en -> new Fragment(en.getKey(), "manuy", en.getValue())));
        bindings.putAll(this.bindings);
        return new Lookup(this.componentName, this.context, bindings, this.fragments);
    }

    public Collection<Fragment> lookupBinding(String zoneName) {
        Fragment fragment = bindings.get(qualify(zoneName));
        if (fragment != null) {
            return Collections.singleton(fragment);
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

    public String getContext() {
        return context;
    }
}
