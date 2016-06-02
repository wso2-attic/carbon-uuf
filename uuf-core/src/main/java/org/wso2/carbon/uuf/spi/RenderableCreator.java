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

package org.wso2.carbon.uuf.spi;

import org.wso2.carbon.uuf.reference.FragmentReference;
import org.wso2.carbon.uuf.reference.LayoutReference;
import org.wso2.carbon.uuf.reference.PageReference;

import java.util.Optional;
import java.util.Set;

public interface RenderableCreator {

    Set<String> getSupportedFileExtensions();

    FragmentRenderableData createFragmentRenderable(FragmentReference fragmentReference, ClassLoader classLoader);

    PageRenderableData createPageRenderable(PageReference pageReference, ClassLoader classLoader);

    LayoutRenderableData createLayoutRenderable(LayoutReference layoutReference);

    int hashCode();

    boolean equals(Object obj);

    class FragmentRenderableData {

        private final Renderable renderable;
        private final boolean isSecured;

        public FragmentRenderableData(Renderable renderable, boolean isSecured) {
            this.renderable = renderable;
            this.isSecured = isSecured;
        }

        public Renderable getRenderable() {
            return renderable;
        }

        public boolean isSecured() {
            return isSecured;
        }
    }

    class PageRenderableData {

        private final Renderable renderable;
        private final boolean isSecured;
        private final String layoutName;

        public PageRenderableData(Renderable renderable, boolean isSecured) {
            this(renderable, isSecured, null);
        }

        public PageRenderableData(Renderable renderable, boolean isSecured, String layoutName) {
            this.renderable = renderable;
            this.isSecured = isSecured;
            this.layoutName = layoutName;
        }

        public Renderable getRenderable() {
            return renderable;
        }

        public boolean isSecured() {
            return isSecured;
        }

        public Optional<String> getLayoutName() {
            return Optional.ofNullable(layoutName);
        }
    }

    class LayoutRenderableData {

        private final Renderable renderable;

        public LayoutRenderableData(Renderable renderable) {
            this.renderable = renderable;
        }

        public Renderable getRenderable() {
            return renderable;
        }
    }
}
