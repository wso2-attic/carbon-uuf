package org.wso2.carbon.uuf.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.wso2.carbon.uuf.handlebars.Executable;

import java.util.Map;
import java.util.Optional;

public class Page {

    private final UriPatten uriPatten;
    private final Renderable layout;
    private final Map<String, Renderable> fillZones;
    private Optional<Executable> script;

    public Page(UriPatten uriPatten, Renderable layout, Map<String, Renderable> fillZones,
                Optional<Executable> script) {
        this.uriPatten = uriPatten;
        this.layout = layout;
        this.fillZones = fillZones;
        this.script = script;
    }

    public UriPatten getUriPatten() {
        return uriPatten;
    }

    public String serve(Object model, Map<String, Renderable> bindings, Map<String, Fragment> fragments) {
        Multimap<String, Renderable> combined = ArrayListMultimap.create();
        // add bindings
        for (Map.Entry<String, Renderable> entry : bindings.entrySet()) {
            combined.put(entry.getKey(), entry.getValue());
        }
        // add fill zones
        for (Map.Entry<String, Renderable> entry : fillZones.entrySet()) {
            combined.put(entry.getKey(), entry.getValue());
        }
        if (script.isPresent()) {
            script.get().execute();
        }
        return layout.render(model, combined, fragments);
    }

    @Override
    public String toString() {
        return "{\"uriPattern\": \"" + uriPatten.toString() + "\", \"layout\": \"" +
                layout.toString() + "\"}";
    }
}
