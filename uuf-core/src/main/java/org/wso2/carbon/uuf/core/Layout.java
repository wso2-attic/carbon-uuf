package org.wso2.carbon.uuf.core;

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

    public String render(ComponentLookup componentLookup, RequestLookup requestLookup) {
        requestLookup.pushToPublicUriStack(requestLookup.getAppContext() + componentLookup.getPublicUriInfix(this));
        String output = renderer.render(null, componentLookup, requestLookup, null);
        requestLookup.popPublicUriStack();
        return output;
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"renderer\": " + renderer + "}";
    }
}
