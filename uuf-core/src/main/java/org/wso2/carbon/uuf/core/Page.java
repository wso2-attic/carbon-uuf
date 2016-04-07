package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.model.Model;

public class Page implements Comparable<Page> {

    private final UriPatten uriPatten;
    private final Renderable renderer;
    private final String publicUriInfix;

    @Deprecated
    public Page(UriPatten uriPatten, Renderable layout, Lookup lookup) {
        this.uriPatten = uriPatten;
        this.renderer = layout;
        this.publicUriInfix = null;
    }

    public Page(UriPatten uriPatten, String publicUriInfix, Renderable renderer) {
        this.uriPatten = uriPatten;
        this.publicUriInfix = publicUriInfix;
        this.renderer = renderer;
    }

    public UriPatten getUriPatten() {
        return uriPatten;
    }

    public String getPublicUriInfix() {
        return publicUriInfix;
    }

    public String render(Model model, StaticLookup staticLookup, RequestLookup requestLookup, API api) {
        requestLookup.getPagesStack().push(this);
        String output = renderer.render(model, staticLookup, requestLookup, api);
        requestLookup.getPagesStack().pop();
        return output;
    }

    @Deprecated
    public String serve(String uriUpToContext, Model model) {
        return "";
    }

    @Override
    public String toString() {
        return "{\"uriPattern\": \"" + uriPatten.toString() + "\", \"publicUriPrefix\": \"" + publicUriInfix + "\"}";
    }

    @Override
    public int compareTo(Page otherPage) {
        return this.getUriPatten().compareTo(otherPage.getUriPatten());
    }
}
