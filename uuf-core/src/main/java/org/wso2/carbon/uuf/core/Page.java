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

import org.wso2.carbon.uuf.exception.SessionNotFoundException;
import org.wso2.carbon.uuf.internal.core.UriPatten;
import org.wso2.carbon.uuf.internal.util.UriUtils;
import org.wso2.carbon.uuf.spi.Renderable;
import org.wso2.carbon.uuf.spi.model.Model;

public class Page implements Comparable<Page> {

    private final UriPatten uriPatten;
    private final Renderable renderer;
    private final boolean isSecured;
    private final Layout layout;

    public Page(UriPatten uriPatten, Renderable renderer, boolean isSecured) {
        this(uriPatten, renderer, isSecured, null);
    }

    public Page(UriPatten uriPatten, Renderable renderer, boolean isSecured, Layout layout) {
        this.uriPatten = uriPatten;
        this.renderer = renderer;
        this.isSecured = isSecured;
        this.layout = layout;
    }

    public UriPatten getUriPatten() {
        return uriPatten;
    }

    public String render(Model model, Lookup lookup, RequestLookup requestLookup, API api) {
        if (isSecured && !api.getSession().isPresent()) {
            throw new SessionNotFoundException(
                    "Page '" + this + "' is secured and required an user session to render.");
        }

        // Rendering flow tracking in.
        requestLookup.tracker().in(this);
        Component currentComponent = lookup.getComponent(requestLookup.tracker().getCurrentComponentName()).get();
        requestLookup.pushToPublicUriStack(UriUtils.getPublicUri(currentComponent, this));
        String output = renderer.render(model, lookup, requestLookup, api);
        if (layout != null) {
            output = layout.render(lookup, requestLookup, api);
        }
        // Rendering flow tracking out.
        requestLookup.popPublicUriStack();
        requestLookup.tracker().out(this);
        return output;
    }

    @Override
    public int hashCode() {
        return uriPatten.hashCode() + (31 * renderer.hashCode()) + (layout == null ? 0 : (31 * layout.hashCode()));
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
        return "{\"uriPattern\": " + uriPatten + ", \"renderer\": " + renderer + ", \"secured\": " + isSecured +
                (layout == null ? "}" : ", \"layout\": " + layout + "}");
    }
}
