package org.wso2.carbon.uuf.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.model.MapModel;
import org.wso2.carbon.uuf.model.Model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

public class Component {
    public static final String ROOT_COMPONENT_NAME = "root";
    public static final String ROOT_COMPONENT_CONTEXT = "/root";
    private static final Logger log = LoggerFactory.getLogger(Component.class);

    private final String name;
    private final String version;
    private final SortedSet<Page> pages;
    private final ComponentLookup lookup;

    public Component(String name, String version, SortedSet<Page> pages, ComponentLookup lookup) {
        if (!name.equals(lookup.getComponentName())) {
            throw new IllegalArgumentException("Specified 'lookup' does not belong to this component.");
        }
        this.name = name;
        this.version = version;
        this.pages = pages;
        this.lookup = lookup;
    }

    String getName() {
        return name;
    }

    String getContext() {
        return lookup.getComponentContext();
    }

    public Map<String, Fragment> getFragments() {
        return lookup.getFragments();
    }

    ComponentLookup getLookup() {
        return lookup;
    }

    public Optional<String> renderPage(String pageUri, RequestLookup requestLookup, API api) {
        Optional<Page> servingPage = getPage(pageUri);
        if (!servingPage.isPresent()) {
            return Optional.<String>empty();
        }

        Page page = servingPage.get();
        if (log.isDebugEnabled()) {
            log.debug("Component '" + lookup.getComponentName() + "' is serving Page '" + page + "' for URI '" +
                              pageUri + "'.");
        }

        Model model = new MapModel(new HashMap<>());
        return Optional.of(page.render(model, lookup, requestLookup, api));
    }

    private Optional<Page> getPage(String pageUri) {
        return pages.stream().filter(page -> page.getUriPatten().match(pageUri)).findFirst();
    }

    @Deprecated
    public boolean hasPage(String uri) {
        return getPage(uri).isPresent();
    }

    @Deprecated
    public Set<Page> getPages() {
        return new HashSet<>(pages);
    }

    @Override
    public String toString() {
        return "{\"name\":\"" + name + "\", \"version\": \"" + version + "\", \"context\": \"" + getContext() + "\"}";
    }
}
