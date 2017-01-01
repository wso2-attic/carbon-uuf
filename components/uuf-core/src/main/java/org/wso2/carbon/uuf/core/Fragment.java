/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.uuf.core;

import org.wso2.carbon.uuf.exception.SessionNotFoundException;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.internal.util.UriUtils;
import org.wso2.carbon.uuf.spi.Renderable;
import org.wso2.carbon.uuf.spi.model.Model;

import java.util.Objects;

public class Fragment {

    private final String name;
    private final String simpleName;
    private final Renderable renderer;
    private final boolean isSecured;

    /**
     * @param name      fully qualified name
     * @param renderer  renderer
     * @param isSecured secured fragment or not
     */
    public Fragment(String name, Renderable renderer, boolean isSecured) {
        this.name = name;
        this.simpleName = NameUtils.getSimpleName(name);
        this.renderer = renderer;
        this.isSecured = isSecured;
    }

    public String getName() {
        return name;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public Renderable getRenderable() {
        return renderer;
    }

    public String render(Model model, Lookup lookup, RequestLookup requestLookup, API api) {
        if (isSecured && !api.getSession().isPresent()) {
            if (requestLookup.tracker().isInPage() || requestLookup.tracker().isInLayout() ||
                    requestLookup.tracker().isInFragment()) {
                // This fragment is included in a page/fragment/layout which is not secured.
                return "";
            } else {
                // This fragment is called directly.
                throw new SessionNotFoundException(
                        "Fragment '" + name + "' is secured and required an user session to render.");
            }
        }

        // Rendering flow tracking in.
        requestLookup.tracker().in(this);
        lookup.getComponent(requestLookup.tracker().getCurrentComponentName())
                .map(component -> UriUtils.getPublicUri(component, this)) // Compute public URI for this fragment.
                .ifPresent(requestLookup::pushToPublicUriStack); // Push it to the public URi stack.
        String output = renderer.render(model, lookup, requestLookup, api);
        // Rendering flow tracking out.
        requestLookup.popPublicUriStack();
        requestLookup.tracker().out(this);
        return output;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, renderer);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Fragment) && (this.name.equals(((Fragment) obj).name));
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"renderer\": " + renderer + ", \"secured\": " + isSecured + "}";
    }
}
