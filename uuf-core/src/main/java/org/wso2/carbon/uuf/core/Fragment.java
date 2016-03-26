package org.wso2.carbon.uuf.core;

import com.google.common.collect.Multimap;

import java.util.Map;

public class Fragment {

    private final String name;
    private final String resourceUriPrefix;
    private final Renderable renderer;

    @Deprecated
    public Fragment(String name, Renderable renderer) {
        //TODO remove this constructor
        this(name, ("/public/component-name/" + name), renderer);
    }

    public Fragment(String name, String resourceUriPrefix, Renderable renderer) {
        this.name = name;
        this.resourceUriPrefix = resourceUriPrefix;
        this.renderer = renderer;
    }

    public String getName() {
        return name;
    }

    public String getResourceUriPrefix() {
        return resourceUriPrefix;
    }

    public Renderable getRenderer() {
        return renderer;
    }

    public String render(Object model, Multimap<String, Renderable> bindings, Map<String, Fragment> fragments) {
        return renderer.render(model, bindings, fragments);
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"renderer\": " + renderer.toString() + "}";
    }
}
