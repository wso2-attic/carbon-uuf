package org.wso2.carbon.uuf.core;

import com.google.common.collect.Multimap;

import java.util.Map;

public class Fragment {

    private final String name;
    private final String path;
    private final Renderable renderer;

    public Fragment(String name, String path, Renderable renderer) {
        this.name = name;
        this.path = path;
        this.renderer = renderer;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Renderable getRenderer() {
        return renderer;
    }

    public String render(Object model, Multimap<String, Renderable> bindings, Map<String, Fragment> fragments) {
        return renderer.render(model, bindings, fragments);
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"path\": \"" + path + "\", \"renderer\": \"" + renderer.toString() +
                "\"}";
    }
}
