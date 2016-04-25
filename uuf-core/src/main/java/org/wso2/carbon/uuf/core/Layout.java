package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.model.MapModel;

import java.util.Collections;

public class Layout {
    private final String name;
    private final Renderable renderer;

    public Layout(String name, Renderable renderer) {
        this.name = name;
        this.renderer = renderer;
    }

    public String getName() {
        return name;
    }

    public String render(ComponentLookup componentLookup, RequestLookup requestLookup, API api) {
        //requestLookup.pushToPublicUriStack(requestLookup.getAppContext() + componentLookup.getPublicUriInfix(this));
        String output = renderer.render(new MapModel(Collections.emptyMap()), componentLookup, requestLookup, api);
        requestLookup.popPublicUriStack();
        return output;
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"renderer\": " + renderer + "}";
    }
}
