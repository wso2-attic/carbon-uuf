package org.wso2.carbon.uuf.core;

import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class App {
    public static final String ROOT_COMPONENT_NAME = "root";
    private static final Logger log = LoggerFactory.getLogger(App.class);

    private final String context;
    private final Map<String, Component> components;
    private final Component rootComponent;
    private final Map<String, Renderable> bindings;
    private final Map<String, String> configuration;

    private final List<Page> pages;
    private final Map<String, Fragment> fragments;

    public App(String context, List<Page> pages, Map<String, Fragment> fragments, Map<String, Renderable> bindings,
               Map<String, String> configuration) {
        if (!context.startsWith("/")) {
            throw new IllegalArgumentException("app context must start with a '/'");
        }

        // We sort uri so that more wildcard-ed ones go to the bottom.
        Collections.sort(pages, (o1, o2) -> o1.getUriPatten().compareTo(o2.getUriPatten()));

        this.context = context;
        this.fragments = fragments;
        this.bindings = bindings;
        this.pages = pages;
        this.configuration = configuration;
        components = null;
        rootComponent = null;
    }

    public App(String context, Set<Component> components, Map<String, String> configuration) {
        if (context.isEmpty()) {
            throw new IllegalArgumentException("App context cannot be empty.");
        }
        if (!context.startsWith("/")) {
            context = "/" + context;
        }
        this.context = context;

        this.components = new HashMap<>(components.size());
        this.bindings = new HashMap<>();
        Component rootComponent = null;
        for (Component component : components) {
            this.components.put(component.getContext(), component);
            this.bindings.putAll(component.getBindings());
            if (component.getName().equals(ROOT_COMPONENT_NAME)) {
                rootComponent = component;
            }
        }
        if (rootComponent == null) {
            throw new UUFException("No root component found.");
        }
        this.rootComponent = rootComponent;
        this.configuration = configuration;

        fragments = null;
        pages = null;
    }

    public String renderPage(String uri) {
        String pageUri = uri.substring(context.length());
        // First try to render the page with root component
        Optional<String> output = rootComponent.renderPage(pageUri);
        if (output.isPresent()) {
            return output.get();
        }
        // Since root components doesn't have the page, try with other components
        String componentContext = pageUri.substring(0, pageUri.indexOf('/', 1));
        Component component = components.get(componentContext);
        if (component == null) {
            throw new UUFException("Requested page '" + uri + "' does not exists.", Response.Status.NOT_FOUND);
        }
        output = component.renderPage(pageUri.substring(pageUri.indexOf('/', 1)));
        if (output.isPresent()) {
            return output.get();
        } else {
            throw new UUFException("Requested page '" + uri + "' does not exists.", Response.Status.NOT_FOUND);
        }
    }

    public String renderPage(HttpRequest request) {
        String pageUri = request.getUri().substring(context.length());
        Optional<Page> servingPage = getPage(pageUri);
        if (servingPage.isPresent()) {
            Page page = servingPage.get();
            if (log.isDebugEnabled()) {
                log.debug("Page '" + page.toString() + "' is serving.");
            }

            Map<String, Object> model = new HashMap<>();
            model.put("pageUri", pageUri);
            model.put("config", configuration);

            return page.serve(model, bindings, fragments);
        } else {
            throw new UUFException("Requested page '" + pageUri + "' does not exists.", Response.Status.NOT_FOUND);
        }
    }

    public Optional<Page> getPage(String pageUri) {
        for (Page p : pages) {
            if (p.getUriPatten().match(pageUri)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "{\"context\": \"" + context + "\"}";
    }

    public Collection<Fragment> getFragments() {
        return fragments.values();
    }

    public List<Page> getPages() {
        return Collections.unmodifiableList(pages);
    }
}
