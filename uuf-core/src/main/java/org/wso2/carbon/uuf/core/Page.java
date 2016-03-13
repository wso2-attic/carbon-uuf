package org.wso2.carbon.uuf.core;

import io.netty.handler.codec.http.HttpRequest;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class Page {
    private final UriPatten uri;
    private final Renderable template;
    private final Optional<Executable> script;
    private final Optional<Renderable> layout;

    public Page(UriPatten uri, Renderable template) {
        this(uri, template, Optional.empty(), Optional.empty());
    }


    public Page(UriPatten uri, Renderable template, Optional<Executable> script, Optional<Renderable> layout) {
        this.uri = uri;
        this.template = template;
        this.script = script;
        this.layout = layout;
    }

    public UriPatten getUri() {
        return uri;
    }

    public String serve(HttpRequest request, Map<String, Renderable> fragments) {
        Object model;
        if (script.isPresent()) {
            model = script.get().execute();
        } else {
            model = Collections.EMPTY_MAP;
        }
        if (layout.isPresent()) {
            return layout.get().render(model, template.getFillingZones(), fragments);
        }
        return template.render(model, Collections.emptyMap(), fragments);
    }

    @Override
    public String toString() {
        return "{uri:" + uri + ",template:" + template +
                (script.isPresent() ? ",script:" + script : "") +
                (layout.isPresent() ? ",layout:" + layout + "}" : "}");
    }

}
