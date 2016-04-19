package org.wso2.carbon.uuf.core;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.uuf.core.auth.SessionRegistry;
import org.wso2.carbon.uuf.core.create.AppReference;
import org.wso2.carbon.uuf.fileio.ArtifactAppReference;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class App {
    @Deprecated
    private static final String ROOT_COMPONENT_CONTEXT = "/root";
    private static final String STATIC_RESOURCE_URI_BASE_PREFIX = "base";
    private static final String STATIC_RESOURCE_URI_PREFIX = "public";
    private final String context;
    private final Map<String, Component> components;
    private final Component rootComponent;
    private final SessionRegistry sessionRegistry;
    private final API api;

    public App(String context, Set<Component> components, SessionRegistry sessionRegistry) {
        if (!context.startsWith("/")) {
            throw new IllegalArgumentException("App context must start with a '/'.");
        }

        this.context = context;
        this.components = components.stream().collect(Collectors.toMap(Component::getContext, cmp -> cmp));
        this.rootComponent = this.components.get(ROOT_COMPONENT_CONTEXT);
        this.sessionRegistry = sessionRegistry;
        this.api = new API(sessionRegistry);
    }

    @Deprecated
    public App(String context, Set<Component> components) {
        this.context = context;
        this.components = null;
        this.rootComponent = null;
        this.sessionRegistry = null;
        this.api = null;
    }

    public String renderPage(String uriWithoutContext, RequestLookup requestLookup) {
        // First try to render the page with root component
        Optional<String> output = rootComponent.renderPage(uriWithoutContext, requestLookup, api);
        if (output.isPresent()) {
            return output.get();
        }

        // Since root components doesn't have the page, try with other components
        for (Map.Entry<String, Component> entry : components.entrySet()) {
            if (uriWithoutContext.startsWith(entry.getKey())) {
                Component component = entry.getValue();
                String pageUri = uriWithoutContext.substring(component.getName().length());
                output = component.renderPage(pageUri, requestLookup, api);
                if (output.isPresent()) {
                    return output.get();
                }
                break;
            }
        }
        throw new UUFException("Requested page '" + uriWithoutContext + "' does not exists.",
                               Response.Status.NOT_FOUND);
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
