package org.wso2.carbon.uuf.core;

import java.util.Map;
import java.util.Optional;

public class Page {

    private final String name;
    private final String path;
    private final UriPatten uriPatten;
    private final Renderable renderer;
    private final Optional<Renderable> layout;

    public Page(String name, String path, UriPatten uriPatten, Renderable renderer) {
        this(name, path, uriPatten, renderer, Optional.empty());
    }

    public Page(String name, String path, UriPatten uriPatten, Renderable renderer, Optional<Renderable> layout) {
        this.name = name;
        this.path = path;
        this.uriPatten = uriPatten;
        this.renderer = renderer;
        this.layout = layout;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public UriPatten getUriPatten() {
        return uriPatten;
    }

    public String serve(Map model, Map<String, Fragment> bindings, Map<String, Fragment> fragments) {
        String tmp = renderer.render(model, bindings, fragments);
        if (layout.isPresent()) {
            return layout.get().render(model, bindings, fragments);
        }
        return tmp;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Page) && (this.name.equals(((Page) obj).name));
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"path\": \"" + path + "\", \"uriPattern\": \"" + uriPatten.toString() +
                "\", \"renderer\": \"" + renderer.toString() + "\", \"layout\": \"" + layout.toString() + "\"}";
    }

}
