package org.wso2.carbon.uuf.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.model.MapModel;

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
    private final Lookup lookup;

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
    }

    public String getContext() {
        return context;
    }

    public Optional<String> renderPage(String pageUri) {
        Optional<Page> servingPage = getPage(pageUri);
        if (log.isDebugEnabled() && servingPage.isPresent()) {
            log.debug("Component '" + name + "' is serving Page '" +
                    servingPage.get().toString() + "' for URI '" + pageUri + "'.");
        }
        return servingPage.map(page -> {
            MapModel model = new MapModel(createModel(pageUri));
            return page.serve(pageUri, model, lookup);
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
        //TODO: convert pages to a set
        return new HashSet<>(pages);
    }

    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public String toString() {
        return "{name:\"" + name + "\"}";
    }
}
