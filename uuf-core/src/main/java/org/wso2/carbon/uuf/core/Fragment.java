package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.model.Model;

public class Fragment {

    private final String name;
    private final String publicUriInfix;
    private final Renderable renderer;

    public Fragment(String name, String publicUriInfix, Renderable renderer) {
        this.name = name;
        this.publicUriInfix = publicUriInfix;
        this.renderer = renderer;
    }

    public String getName() {
        return name;
    }

    public String getPublicUriInfix() {
        return publicUriInfix;
    }

    public Renderable getRenderer() {
        return renderer;
    }

    public String render(Model model, StaticLookup staticLookup, RequestLookup requestLookup, API api) {
        requestLookup.getFragmentsStack().push(this);
        String output = renderer.render(model, staticLookup, requestLookup, api);
        requestLookup.getFragmentsStack().pop();
        return output;
    }

    @Deprecated
    public String render(String uri, Model model, Lookup lookup) {
        return "";
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"publicUriInfix\": \"" + publicUriInfix + "\"}";
    }

    @Deprecated
    public String getPublicContext() {
        return publicUriInfix;
    }
}
