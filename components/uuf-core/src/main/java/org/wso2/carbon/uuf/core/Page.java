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
import org.wso2.carbon.uuf.exception.SessionNotFoundException;
import org.wso2.carbon.uuf.exception.UnauthorizedException;
import org.wso2.carbon.uuf.internal.debug.DebugLogger;
import org.wso2.carbon.uuf.internal.util.UriUtils;
import org.wso2.carbon.uuf.spi.Renderable;
import org.wso2.carbon.uuf.spi.model.Model;

import java.util.Objects;

public class Page implements Comparable<Page> {

    private final UriPatten uriPatten;
    private final Renderable renderer;
    private final Permission permission;
    private final Layout layout;

    public Page(UriPatten uriPatten, Renderable renderer, Permission permission) {
        this(uriPatten, renderer, permission, null);
    }

    public Page(UriPatten uriPatten, Renderable renderer, Permission permission, Layout layout) {
        this.uriPatten = uriPatten;
        this.renderer = renderer;
        this.permission = permission;
        this.layout = layout;
    }

    public UriPatten getUriPatten() {
        return uriPatten;
    }

    public String render(Model model, Lookup lookup, RequestLookup requestLookup, API api) {
        if (permission != null) {
            if (!api.getSession().isPresent()) {
                throw new SessionNotFoundException(
                        "Page '" + this + "' is secured and required an user session to render.");
            }
            if (!api.hasPermission(permission)) {
                throw new UnauthorizedException("You do not have enough permission to view this page.");
            }
        }

        try {
            // Debug logs for page rendering start.
            DebugLogger.startPage(this);
            // Rendering flow tracking in.
            requestLookup.tracker().in(this);
            lookup.getComponent(requestLookup.tracker().getCurrentComponentName())
                    .map(component -> UriUtils.getPublicUri(component, this)) // Compute public URI for this page.
                    .ifPresent(requestLookup::pushToPublicUriStack); // Push it to the public URi stack.

            String output = renderer.render(model, lookup, requestLookup, api);
            if (layout != null) {
                output = layout.render(lookup, requestLookup, api);
            }
            return output;
        } finally {
            // Rendering flow tracking out.
            requestLookup.popPublicUriStack();
            requestLookup.tracker().out(this);
            // Debug logs for page rendering end.
            DebugLogger.endPage(this);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(uriPatten, renderer, layout);
    }

    @Override
    public int compareTo(Page otherPage) {
        return (otherPage == null) ? 1 : this.getUriPatten().compareTo(otherPage.getUriPatten());
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Page) && (this.compareTo((Page) obj) == 0);
    }

    @Override
    public String toString() {
        return "{\"uriPattern\": " + uriPatten + ", \"renderer\": " + renderer + ", \"permission\": " + permission +
                (layout == null ? "}" : ", \"layout\": " + layout + "}");
    }
}
