package org.wso2.carbon.uuf.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

//    public App(String context, List<Page> pages, Map<String, Fragment> fragments, Map<String, Renderable> bindings,
//               Map<String, String> configuration) {
//        if (!context.startsWith("/")) {
//            throw new IllegalArgumentException("app context must start with a '/'");
//        }
//
//        // We sort uri so that more wildcard-ed ones go to the bottom.
//        Collections.sort(pages, (o1, o2) -> o1.getUriPatten().compareTo(o2.getUriPatten()));
//
//        this.context = context;
//        this.fragments = fragments;
//        this.bindings = bindings;
//        this.pages = pages;
//        this.configuration = configuration;
//        components = null;
//        rootComponent = components.get("/");
//        if (rootComponent == null) {
//            throw new UUFException("App must have a root component");
//        }
//    }

    public App(String context, Map<String, Component> components) {
        if (!context.startsWith("/")) {
            throw new IllegalArgumentException("app context must start with a '/'");
        }

        this.context = context;
        this.components = Collections.unmodifiableMap(components);
        this.rootComponent = components.get("/");
        if (rootComponent == null) {
            throw new UUFException("App must have a root component");
        }

        //TODO: calculate base on the components
        this.configuration = Collections.emptyMap();
        this.bindings = Collections.emptyMap();

        //TODO: remove
        fragments = null;
        pages = null;
    }

    public String renderPage(String uri) {
        // First try to render the page with root component
        Optional<String> output = rootComponent.renderPage(uri);
        if (output.isPresent()) {
            return output.get();
        }

        // Since root components doesn't have the page, try with other components
        int firstSlash = uri.indexOf('/', 1);
        if (firstSlash > 0) {
            String componentContext = uri.substring(0, firstSlash);
            Component component = components.get(componentContext);
            if (component != null) {
                output = component.renderPage(uri.substring(firstSlash));
                if (output.isPresent()) {
                    return output.get();
                }
            }
        }
        throw new UUFException("Requested page '" + uri + "' does not exists.", Response.Status.NOT_FOUND);
    }

    public boolean hasPage(String uri) {
        if (rootComponent.hasPage(uri)) {
            return true;
        }

        int firstSlash = uri.indexOf('/', 1);
        if (firstSlash > 0) {
            String componentContext = uri.substring(0, firstSlash);
            Component component = components.get(componentContext);
            if (component != null) {
                return component.hasPage(uri.substring(firstSlash));
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "{\"context\": \"" + context + "\"}";
    }

    public Map<String, Component> getComponents() {
        return components;
    }
}
