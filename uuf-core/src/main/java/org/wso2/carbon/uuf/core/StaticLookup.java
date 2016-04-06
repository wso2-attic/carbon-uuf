package org.wso2.carbon.uuf.core;

import java.util.Map;

public class StaticLookup {
    private final Map<String, ? extends Renderable> bindings;
    private final Map<String, Fragment> fragments;
    private final Map<String, Object> configurations;

    public StaticLookup(Map<String, ? extends Renderable> bindings,
                        Map<String, Fragment> fragments, Map<String, Object> configurations) {
        this.bindings = bindings;
        this.fragments = fragments;
        this.configurations = configurations;
    }

    public Map<String, ? extends Renderable> getBindings() {
        return bindings;
    }

    public Map<String, Fragment> getFragments() {
        return fragments;
    }

    public Map<String, Object> getConfigurations() {
        return configurations;
    }
}
