package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.model.Model;

import java.util.Optional;

public class Page implements Comparable<Page> {

    private final UriPatten uriPatten;
    private final Renderable renderer;
    private final Optional<Layout> layout;

    public Page(UriPatten uriPatten, Renderable renderer) {
        this(uriPatten, renderer, null);
    }

    public Page(UriPatten uriPatten, Renderable renderer, Layout layout) {
        this.uriPatten = uriPatten;
        this.renderer = renderer;
        this.layout = Optional.ofNullable(layout);
    }

    public UriPatten getUriPatten() {
        return uriPatten;
    }

    public String render(Model model, ComponentLookup componentLookup, RequestLookup requestLookup, API api) {
        requestLookup.pushToPublicUriStack(requestLookup.getAppContext() + componentLookup.getPublicUriInfix(this));
        String output = renderer.render(model, componentLookup, requestLookup, api);
        requestLookup.popPublicUriStack();
        if (layout.isPresent()) {
            output = layout.get().render(componentLookup, requestLookup);
        }
        return output;
    }

    @Override
    public String toString() {
        return "{\"uriPattern\": " + uriPatten + ", \"renderer\": " + renderer + ", \"layout\": " + layout.get() + "}";
    }

    @Override
    public int compareTo(Page otherPage) {
        return this.getUriPatten().compareTo(otherPage.getUriPatten());
    }
}
