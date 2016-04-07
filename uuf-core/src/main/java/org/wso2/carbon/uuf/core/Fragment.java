package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.model.Model;

public class Fragment {

    private final String name;
    private final String publicUriPrefix;
    private final Renderable renderer;

    public Fragment(String name, String publicUriPrefix, Renderable renderer) {
        this.name = name;
        this.publicUriPrefix = publicUriPrefix;
        this.renderer = renderer;
    }

    public String getName() {
        return name;
    }

    public String getPublicUriPrefix() {
        return publicUriPrefix;
    }

    public Renderable getRenderer() {
        return renderer;
    }

    public String render(Model model, StaticLookup staticLookup, DynamicLookup dynamicLookup, API uufCaller) {
        dynamicLookup.getFragmentsStack().push(this);
        String output = renderer.render(model, staticLookup, dynamicLookup, uufCaller);
        dynamicLookup.getFragmentsStack().pop();
        return output;
    }

    @Deprecated
    public String render(String uri, Model model, Lookup lookup) {
        return "";
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"publicUriPrefix\": \"" + publicUriPrefix + "\"}";
    }

    @Deprecated
    public String getPublicContext() {
        return publicUriPrefix;
    }
}
