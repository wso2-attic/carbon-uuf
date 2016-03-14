package org.wso2.carbon.uuf.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collections;
import java.util.Map;

public class Page {

    private final String path;
    private final UriPatten uriPatten;
    private final Renderable layout;
    private final Map<String, Renderable> fillZones;

    public Page(String path, UriPatten uriPatten, Renderable layout) {
        this(path, uriPatten, layout, Collections.emptyMap());
    }

    public Page(String path, UriPatten uriPatten, Renderable layout, Map<String, Renderable> fillZones) {
        this.path = path;
        this.uriPatten = uriPatten;
        this.layout = layout;
        this.fillZones = fillZones;
    }

    public String getPath() {
        return path;
    }

    public UriPatten getUriPatten() {
        return uriPatten;
    }

    public String serve(Map model, Map<String, Renderable> bindings, Map<String, Fragment> fragments) {
        Multimap<String, Renderable> combined = ArrayListMultimap.create();
        // add bindings
        for (Map.Entry<String, Renderable> entry : bindings.entrySet()) {
            combined.put(entry.getKey(), entry.getValue());
        }
        // add fill zones
        for (Map.Entry<String, Renderable> entry : fillZones.entrySet()) {
            combined.put(entry.getKey(), entry.getValue());
        }
        return layout.render(model, combined, fragments);
    }

    @Override
    public String toString() {
        return "{\"path\": \"" + path + "\", \"uriPattern\": \"" + uriPatten.toString() + "\", \"layout\": \"" +
                layout.toString() + "\"}";
    }
}
