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

import org.wso2.carbon.uuf.api.auth.Permission;
import org.wso2.carbon.uuf.internal.debug.DebugLogger;
import org.wso2.carbon.uuf.internal.exception.UnauthorizedException;
import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.internal.util.UriUtils;
import org.wso2.carbon.uuf.spi.Renderable;
import org.wso2.carbon.uuf.spi.model.Model;

import java.util.Objects;

public class Fragment {

    private final String name;
    private final String simpleName;
    private final Renderable renderer;
    private final Permission permission;

    /**
     * Constructs an UUF fragment.
     *
     * @param name       fully qualified name
     * @param renderer   renderer
     * @param permission permission of this fragment
     */
    public Fragment(String name, Renderable renderer, Permission permission) {
        this.name = name;
        this.simpleName = NameUtils.getSimpleName(name);
        this.renderer = renderer;
        this.permission = permission;
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
        if ((permission != null) && (!api.hasPermission(permission))) {
            if (requestLookup.tracker().isInPage() || requestLookup.tracker().isInLayout() ||
                    requestLookup.tracker().isInFragment()) {
                // This fragment is included in a page/fragment/layout which is not secured.
                return "";
            } else {
                throw new UnauthorizedException("You do not have enough permission to view this fragment '" + name
                                                        + "'.");
            }
        }

        try {
            // Debug logs for fragment rendering start.
            DebugLogger.startFragment(this);
            // Rendering flow tracking in.
            requestLookup.tracker().in(this);
            lookup.getComponent(requestLookup.tracker().getCurrentComponentName())
                    .map(component -> UriUtils.getPublicUri(component, this)) // Compute public URI for this fragment.
                    .ifPresent(requestLookup::pushToPublicUriStack); // Push it to the public URi stack.

            return renderer.render(model, lookup, requestLookup, api);
        } finally {
            // Rendering flow tracking out.
            requestLookup.popPublicUriStack();
            requestLookup.tracker().out(this);
            // Debug logs for fragment rendering end.
            DebugLogger.endFragment(this);
        }
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
        return "{\"name\": \"" + name + "\", \"renderer\": " + renderer + ", \"permission\": " + permission + "}";
    }
}
