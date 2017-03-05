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

import org.wso2.carbon.uuf.internal.util.NameUtils;
import org.wso2.carbon.uuf.internal.util.UriUtils;
import org.wso2.carbon.uuf.spi.Renderable;

import java.util.Objects;

public class Layout {

    private final String name;
    private final String simpleName;
    private final Renderable renderer;

    /**
     * @param name     fully qualified name
     * @param renderer renderer
     */
    public Layout(String name, Renderable renderer) {
        this.name = name;
        this.simpleName = NameUtils.getSimpleName(name);
        this.renderer = renderer;
    }

    public String getName() {
        return name;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public String render(Lookup lookup, RequestLookup requestLookup, API api) {
        try {
            // Rendering flow tracking in.
            requestLookup.tracker().in(this);
            Component currentComponent = lookup.getComponent(requestLookup.tracker().getCurrentComponentName()).get();
            requestLookup.pushToPublicUriStack(UriUtils.getPublicUri(currentComponent, this));

            return renderer.render(null, lookup, requestLookup, api);
        } finally {
            // Rendering flow tracking out.
            requestLookup.popPublicUriStack();
            requestLookup.tracker().out(this);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, renderer);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Layout) && (this.name.equals(((Layout) obj).name));
    }

    @Override
    public String toString() {
        return "{\"name\": \"" + name + "\", \"renderer\": " + renderer + "}";
    }
}
