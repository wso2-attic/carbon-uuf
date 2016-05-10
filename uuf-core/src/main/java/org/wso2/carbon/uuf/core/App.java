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

import io.netty.handler.codec.http.QueryStringDecoder;
import org.wso2.carbon.uuf.internal.UUFRegistry;
import org.wso2.carbon.uuf.internal.core.auth.SessionRegistry;
import org.wso2.carbon.uuf.core.exception.FragmentNotFoundException;
import org.wso2.carbon.uuf.core.exception.PageNotFoundException;
import org.wso2.carbon.uuf.api.HttpRequest;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.model.MapModel;
import org.wso2.carbon.uuf.spi.model.Model;

import java.util.HashMap;
import java.util.List;
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
        API api = new API(sessionRegistry, requestLookup);
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

    /**
     * Returns rendered output of this fragment uri. This method intended to use for serving AJAX requests.
     *
     * @param uriWithoutAppContext fragment uri
     * @param requestLookup        request lookup
     * @return rendered output
     */
    public String renderFragment(String uriWithoutAppContext, RequestLookup requestLookup) {
        API api = new API(sessionRegistry, requestLookup);
        int queryParamsPos = uriWithoutAppContext.indexOf("?");
        String fragmentName = (queryParamsPos > -1) ?
                uriWithoutAppContext.substring(UUFRegistry.FRAGMENTS_URI_PREFIX.length(), queryParamsPos) :
                uriWithoutAppContext.substring(UUFRegistry.FRAGMENTS_URI_PREFIX.length());
        if (!NameUtils.isFullyQualifiedName(fragmentName)) {
            fragmentName = NameUtils.getFullyQualifiedName(Component.ROOT_COMPONENT_NAME, fragmentName);
        }

        Model model = createModel(requestLookup.getRequest());
        // First try to render the fragment with root component.
        ComponentLookup componentLookup = rootComponent.getLookup();
        Optional<Fragment> fragment = rootComponent.getLookup().getFragment(fragmentName);
        if (fragment.isPresent()) {
            return fragment.get().render(model, componentLookup, requestLookup, api);
        }

        // Since root components doesn't have the fragment, try with other components.
        String componentName = NameUtils.getComponentName(fragmentName);
        for (Map.Entry<String, Component> entry : components.entrySet()) {
            if (componentName.startsWith(entry.getKey())) {
                Component component = entry.getValue();
                componentLookup = component.getLookup();
                fragment = componentLookup.getFragment(fragmentName);
                if (fragment.isPresent()) {
                    return fragment.get().render(new MapModel(new HashMap<>()), componentLookup, requestLookup, api);
                }
                break;
            }
        }
        throw new FragmentNotFoundException("Requested fragment '" + uriWithoutAppContext + "' does not exists.");
    }

    private Model createModel(HttpRequest httpRequest) {
        QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.getRequestURI());
        Map<String, List<String>> parameters = decoder.parameters();
        Map<String, Object> queryParams = parameters.entrySet().parallelStream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                                          entry -> {
                                              List<String> value = entry.getValue();
                                              return (value.size() > 1) ? value : value.get(0);
                                          }));
        return new MapModel(queryParams);
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
