package org.wso2.carbon.uuf.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Map;

public class Page {

    private final UriPatten uriPatten;
    private final Renderable layout;
    private final Map<String, ? extends Renderable> fillZones;

    public Page(UriPatten uriPatten, Renderable layout, Map<String, ? extends Renderable> fillZones) {
        this.uriPatten = uriPatten;
        this.layout = layout;
        this.fillZones = fillZones;
    }

    public UriPatten getUriPatten() {
        return uriPatten;
    }

    public String serve(Map<String, Object> model, Map<String, Renderable> bindings, Map<String, Fragment> fragments) {
        Multimap<String, Renderable> combined = ArrayListMultimap.create();
        // add bindings
        for (Map.Entry<String, Renderable> entry : bindings.entrySet()) {
            combined.put(entry.getKey(), entry.getValue());
        }
        // add fill zones
        for (Map.Entry<String, ? extends Renderable> entry : fillZones.entrySet()) {
            combined.put(entry.getKey(), entry.getValue());
        }
        return layout.render(model, combined, fragments);
    }

    @Override
    public String toString() {
        return "{\"uriPattern\": " + uriPatten.toString() + ", \"layout\": " + layout.toString() + "}";
    }
}
