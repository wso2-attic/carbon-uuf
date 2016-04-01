package org.wso2.carbon.uuf.core;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class App {
    private final String context;
    private final Map<String, Component> components;
    private final Component rootComponent;

    public App(String context, Set<Component> components) {
        if (!context.startsWith("/")) {
            throw new IllegalArgumentException("app context must start with a '/'");
        }

        this.context = context;
        this.components = components.stream().collect(Collectors.toMap(Component::getContext, Function.identity()));
        this.rootComponent = this.components.get("/root");
    }

    public String renderPage(String uriUpToContext, String uriAfterContext) {
        // First try to render the page with root component
        Optional<String> output = rootComponent.renderPage(uriUpToContext, uriAfterContext);
        if (output.isPresent()) {
            return output.get();
        }

        // Since root components doesn't have the page, try with other components
        int firstSlash = uriAfterContext.indexOf('/', 1);
        if (firstSlash > 0) {
            String componentContext = uriAfterContext.substring(0, firstSlash);
            Component component = components.get(componentContext);
            if (component != null) {
                output = component.renderPage(uriUpToContext, uriAfterContext.substring(firstSlash));
                if (output.isPresent()) {
                    return output.get();
                }
            }
        }
        throw new UUFException("Requested page '" + uriAfterContext + "' does not exists.", Response.Status.NOT_FOUND);
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
