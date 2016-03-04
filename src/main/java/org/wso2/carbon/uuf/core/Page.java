package org.wso2.carbon.uuf.core;

import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

public class Page {
    private static final Logger log = LoggerFactory.getLogger(Page.class);
    private final UriPatten uri;
    private final Renderble template;
    @Nullable
    private final Executable script;
    @Nullable
    private Renderble layout;

    public Page(UriPatten uri, Renderble template) {
        this(uri, template, null, null);
    }


    public Page(UriPatten uri, Renderble template, @Nullable Executable script, @Nullable Renderble layout) {
        this.uri = uri;
        this.template = template;
        this.script = script;
        this.layout = layout;
    }

    public UriPatten getUri() {
        return uri;
    }

    public String serve(HttpRequest request, Map<String, Fragment> frags) {
        Object model;
        if (script != null) {
            model = script.execute();
        } else {
            model = Collections.EMPTY_MAP;
        }
        if (layout != null) {
           return layout.render(model, template.getFillingZones());
        }
        return template.render(model);
    }

    @Override
    public String toString() {
        return "{uri:'" + uri + "',template:" + template +
                (script != null ? ",script:" + script + "}" : "}");
    }

}
