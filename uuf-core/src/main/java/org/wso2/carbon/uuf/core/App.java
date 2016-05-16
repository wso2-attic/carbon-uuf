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

import org.wso2.carbon.uuf.api.model.MapModel;
import org.wso2.carbon.uuf.exception.FragmentNotFoundException;
import org.wso2.carbon.uuf.exception.PageNotFoundException;
import org.wso2.carbon.uuf.internal.core.auth.SessionRegistry;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.internal.util.RequestUtil;
import org.wso2.carbon.uuf.spi.model.Model;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class App {

    private final String name;
    private final String context;
    private final Map<String, Component> components;
    private final Component rootComponent;
    private final SessionRegistry sessionRegistry;

    public App(String name, Set<Component> components, SessionRegistry sessionRegistry) {
        this.name = name;
        this.components = components.stream().collect(Collectors.toMap(Component::getContext, cmp -> cmp));
        this.rootComponent = this.components.remove(Component.ROOT_COMPONENT_CONTEXT);
        this.context = this.rootComponent.getConfiguration().getAppContext()
                .orElse("/" + NameUtils.getSimpleName(name));
        this.sessionRegistry = sessionRegistry;
    }

    public String getName() {
        return name;
    }

    public String getContext() {
        return context;
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
        String fragmentName = uriWithoutAppContext.substring(RequestUtil.FRAGMENTS_URI_PREFIX.length());
        if(NameUtils.isSimpleName(fragmentName)){
            fragmentName = NameUtils.getFullyQualifiedName(rootComponent.getName(), fragmentName);
        }
        // When building the dependency tree, all fragments are accumulated into the rootComponent.
        Fragment fragment = rootComponent.getAllFragments().get(fragmentName);
        if(fragment == null){
            throw new FragmentNotFoundException("Requested fragment '"+fragmentName+"' does not exists.");
        }

        Model model = new MapModel(requestLookup.getRequest().getQueryParams().entrySet().stream()
                                           .collect(Collectors.toMap(Map.Entry::getKey, entry -> (Object) entry)));
        API api = new API(sessionRegistry, requestLookup);
        return fragment.render(model, rootComponent.getLookup(), requestLookup, api);
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

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"context\": \"" + context + "\"}";
    }
}
