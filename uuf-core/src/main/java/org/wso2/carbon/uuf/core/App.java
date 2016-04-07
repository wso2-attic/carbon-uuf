package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.core.auth.SessionRegistry;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class App {
    private static final String ROOT_COMPONENT_CONTEXT = "/root";
    private final String context;
    private final Map<String, Component> components;
    private final Component rootComponent;
    private final SessionRegistry sessionRegistry;

    public App(String context, Set<Component> components, SessionRegistry sessionRegistry) {
        if (!context.startsWith("/")) {
            throw new IllegalArgumentException("App context must start with a '/'.");
        }

        this.context = context;
        this.components = components.stream().collect(Collectors.toMap(Component::getContext, Function.identity()));
        this.rootComponent = this.components.get(ROOT_COMPONENT_CONTEXT);
        this.sessionRegistry = sessionRegistry;
    }

    @Deprecated
    public App(String context, Set<Component> components) {
        if (!context.startsWith("/")) {
            throw new IllegalArgumentException("app context must start with a '/'");
        }

        this.context = context;
        this.components = components.stream().collect(Collectors.toMap(Component::getContext, Function.identity()));
        this.rootComponent = this.components.get("/root");

        this.sessionRegistry = null;
    }

    public String renderPage(String uriWithoutContext, RequestLookup requestLookup) {
        // First try to render the page with root component
        Optional<String> output = rootComponent.renderPage(uriWithoutContext, requestLookup);
        if (output.isPresent()) {
            return output.get();
        }

        // Since root components doesn't have the page, try with other components
        for (Map.Entry<String, Component> entry : components.entrySet()) {
            if (uriWithoutContext.startsWith(entry.getKey())) {
                Component component = entry.getValue();
                String pageUri = uriWithoutContext.substring(component.getContext().length());
                output = component.renderPage(pageUri, requestLookup);
                if (output.isPresent()) {
                    return output.get();
                }
                break;
            }
        }
        throw new UUFException("Requested page '" + uriWithoutContext + "' does not exists.",
                               Response.Status.NOT_FOUND);
    }

    @Deprecated
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

    public SessionRegistry getSessionRegistry() {
        return sessionRegistry;
    }

    public String getContext() {
        return context;
    }
}
