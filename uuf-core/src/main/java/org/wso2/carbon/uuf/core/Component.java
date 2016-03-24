package org.wso2.carbon.uuf.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

public class Component {
    private static final Logger log = LoggerFactory.getLogger(Component.class);

    private final String name;
    private final String context;
    private final SortedSet<Page> pages;
    private final Map<String, Fragment> fragments;
    private final Map<String, String> configuration;
    private final Map<String, Renderable> bindings;

    public Component(String name,
                     String context,
                     SortedSet<Page> pages,
                     Set<Fragment> fragments,
                     Map<String, String> componentConfig,
                     Map<String, String> bindingsConfig) {

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Component name cannot be empty.");
        }

        this.name = name;
        this.context = context;
        this.pages = pages;
        this.configuration = componentConfig;
        this.fragments = fragments.stream().collect(
                Collectors.toMap(Fragment::getName, fragment -> fragment));

        this.bindings = new HashMap<>(bindingsConfig.size());
        for (Map.Entry<String, String> entry : bindingsConfig.entrySet()) {
            Fragment fragment = this.fragments.get(entry.getValue());
            if (fragment == null) {
                throw new UUFException("Fragment '" + entry.getValue()
                        + "' does not exists in Component '" + name +
                        "'. Hence cannot bind it to zone '" + entry.getKey() + "'.");
            }
            bindings.put(entry.getKey(), fragment.getRenderer());
        }
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
        return servingPage.map(page -> page.serve(createModel(pageUri), bindings, fragments));
    }

    private Map<String, Object> createModel(String pageUri) {
        Map<String, Object> model = new HashMap<>();
        model.put("pageUri", pageUri);
        model.put("config", configuration);
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

    public Set<Page> getPages() {
        //TODO: convert pages to a set
        return new HashSet<>(pages);
    }

    public Map<String, Fragment> getFragments() {
        return fragments;
    }
}
