/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.core.auth.SessionRegistry;
import org.wso2.carbon.uuf.core.exception.PageNotFoundException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class App {

    private final String context;
    private final Map<String, Component> components;
    private final Component rootComponent;
    private final SessionRegistry sessionRegistry;

    public App(String context, Set<Component> components, SessionRegistry sessionRegistry) {
        if (!context.startsWith("/")) {
            throw new IllegalArgumentException("App context must start with a '/'.");
        }

        this.context = context;
        this.components = components.stream().collect(Collectors.toMap(Component::getContext, cmp -> cmp));
        this.rootComponent = this.components.get(Component.ROOT_COMPONENT_CONTEXT);
        this.sessionRegistry = sessionRegistry;
    }

    public String renderPage(String uriWithoutContext, RequestLookup requestLookup) {
        API api = new API(sessionRegistry);
        // First try to render the page with root component.
        Optional<String> output = rootComponent.renderPage(uriWithoutContext, requestLookup, api);
        if (output.isPresent()) {
            return output.get();
        }

        // Since root components doesn't have the page, try with other components.
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
        throw new PageNotFoundException("Requested page '" + uriWithoutContext + "' does not exists.");
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

    public Map<String, Component> getComponents() {
        return components;
    }

    public SessionRegistry getSessionRegistry() {
        return sessionRegistry;
    }

    public String getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "{\"context\": \"" + context + "\"}";
    }
}
