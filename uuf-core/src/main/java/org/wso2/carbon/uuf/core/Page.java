package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.model.Model;

public class Page implements Comparable<Page> {

    private final UriPatten uriPatten;
    private final Renderable renderer;
    private final String publicUriPrefix;

    @Deprecated
    public Page(UriPatten uriPatten, Renderable layout, Lookup lookup) {
        this.uriPatten = uriPatten;
        this.renderer = layout;
        this.publicUriPrefix = null;
    }

    public Page(UriPatten uriPatten, String publicUriPrefix, Renderable renderer) {
        this.uriPatten = uriPatten;
        this.publicUriPrefix = publicUriPrefix;
        this.renderer = renderer;
    }

    public UriPatten getUriPatten() {
        return uriPatten;
    }

    public String getPublicUriPrefix() {
        return publicUriPrefix;
    }

    public String render(Model model, StaticLookup staticLookup, DynamicLookup dynamicLookup, API uufCaller) {
        dynamicLookup.getPagesStack().push(this);
        String output = renderer.render(model, staticLookup, dynamicLookup, uufCaller);
        dynamicLookup.getPagesStack().pop();
        return output;
    }

    @Deprecated
    public String serve(String uriUpToContext, Model model) {
        return "";
    }

    @Override
    public String toString() {
        return "{\"uriPattern\": \"" + uriPatten.toString() + "\", \"publicUriPrefix\": \"" + publicUriPrefix + "\"}";
    }

    @Override
    public int compareTo(Page otherPage) {
        return this.getUriPatten().compareTo(otherPage.getUriPatten());
    }
}
