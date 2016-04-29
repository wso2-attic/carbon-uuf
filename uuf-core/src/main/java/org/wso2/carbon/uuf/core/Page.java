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
    public int hashCode() {
        return uriPatten.hashCode() + (31 * renderer.hashCode()) + (layout.isPresent() ? (31 * layout.hashCode()) : 0);
    }

    @Override
    public int compareTo(Page otherPage) {
        return (otherPage == null) ? 1 : this.getUriPatten().compareTo(otherPage.getUriPatten());
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (!(obj instanceof Page)) && (this.compareTo((Page) obj) == 0);
    }

    @Override
    public String toString() {
        return "{\"uriPattern\": " + uriPatten + ", \"renderer\": " + renderer + ", \"layout\": " + layout.get() + "}";
    }
}
