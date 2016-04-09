package org.wso2.carbon.uuf.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.model.MapModel;
import org.wso2.carbon.uuf.model.Model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

public class Component {
    private static final Logger log = LoggerFactory.getLogger(Component.class);

    private final String name;
    private final String context;
    private final SortedSet<Page> pages;
    private final String version;

    ComponentLookup getComponentLookup() {
        return componentLookup;
    }

    private final ComponentLookup componentLookup;
    @Deprecated
    private final Lookup lookup;

    public Component(String name, String version, String context, SortedSet<Page> pages, ComponentLookup componentLookup) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Component name cannot be empty.");
        }
        this.name = name;
        this.version = version;
        if (!context.startsWith("/")) {
            throw new IllegalArgumentException("Component context must start with a '/'.");
        }
        this.context = context;
        this.pages = pages;
        this.componentLookup = componentLookup;
        this.lookup = null;
    }

    @Deprecated
    public Component(String name,
                     String context,
                     String version,
                     SortedSet<Page> pages,
                     Lookup lookup) {

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Component name cannot be empty.");
        }

        this.name = name;
        this.context = context;
        this.version = version;
        this.pages = pages;
        this.lookup = lookup;
        this.componentLookup = null;
    }

    public String getContext() {
        return context;
    }

    public Optional<String> renderPage(String pageUri, RequestLookup requestLookup) {
        Optional<Page> servingPage = getPage(pageUri);
        if (!servingPage.isPresent()) {
            return Optional.<String>empty();
        }

        Page page = servingPage.get();
        if (log.isDebugEnabled()) {
            log.debug("Component '" + name + "' is serving Page '" + page + "' for URI '" + pageUri + "'.");
        }

        Model model = new MapModel(Collections.emptyMap());
        return Optional.of(page.render(model, componentLookup, requestLookup, null));
    }

    @Deprecated
    public Optional<String> renderPage(String uriUpToContext, String pageUri) {
        Optional<Page> servingPage = getPage(pageUri);
        if (log.isDebugEnabled() && servingPage.isPresent()) {
            log.debug("Component '" + name + "' is serving Page '" +
                              servingPage.get().toString() + "' for URI '" + pageUri + "'.");
        }
        return servingPage.map(page -> {
            MapModel model = new MapModel(createModel(uriUpToContext + '/' + pageUri));
            return page.serve(uriUpToContext, model);
        });
    }

    private Map<String, Object> createModel(String pageUri) {
        Map<String, Object> model = new HashMap<>();
        model.put("pageUri", pageUri);
        return model;
    }

    private Optional<Page> getPage(String pageUri) {
        return pages.stream().filter(page -> page.getUriPatten().match(pageUri)).findFirst();
    }

    public boolean hasPage(String uri) {
        return getPage(uri).isPresent();
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Set<Page> getPages() {
        return new HashSet<>(pages);
    }

    @Deprecated
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public String toString() {
        return "{\"name\":\"" + name + "\", \"version\":\"" + version + "\", \"context\": \"" + context + "\"}";
    }
}
