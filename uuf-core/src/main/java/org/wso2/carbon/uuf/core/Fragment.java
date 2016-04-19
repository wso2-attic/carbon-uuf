package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.model.Model;

public class Fragment {

    private final String name;
    private final Renderable renderer;

    public Fragment(String name, Renderable renderer) {
        this.name = name;
        this.renderer = renderer;
    }

    @Deprecated
    public Fragment(String name, String publicUriInfix, Renderable renderer) {
        this.name = name;
        this.renderer = renderer;
    }

    public String getName() {
        return name;
    }

    public String render(Model model, ComponentLookup componentLookup, RequestLookup requestLookup, API api) {
        requestLookup.pushToPublicUriStack(requestLookup.getAppContext() + componentLookup.getPublicUriInfix(this));
        String output = renderer.render(model, componentLookup, requestLookup, api);
        requestLookup.popPublicUriStack();
        return output;
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"renderer\": \"" + renderer + "\"}";
    }

    @Deprecated
    public String getPublicContext() {
        return "";
    }

    public Renderable getRenderer() {
        return renderer;
    }
}
