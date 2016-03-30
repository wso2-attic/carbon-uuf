package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.model.Model;

public class Fragment {

    private final String name;
    private final String resourceUriPrefix;
    private final Renderable renderer;

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

    public String render(String uri, Model model, Lookup lookup) {
        return renderer.render(uri, model, lookup);
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"renderer\": " + renderer.toString() + "}";
    }
}
