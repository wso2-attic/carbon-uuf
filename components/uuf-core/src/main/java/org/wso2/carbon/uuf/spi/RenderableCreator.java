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

package org.wso2.carbon.uuf.spi;

import org.wso2.carbon.uuf.api.auth.Permission;
import org.wso2.carbon.uuf.api.exception.RenderableCreationException;
import org.wso2.carbon.uuf.api.reference.FragmentReference;
import org.wso2.carbon.uuf.api.reference.LayoutReference;
import org.wso2.carbon.uuf.api.reference.PageReference;

import java.util.Optional;
import java.util.Set;

public interface RenderableCreator {

    Set<String> getSupportedFileExtensions();

    FragmentRenderableData createFragmentRenderable(FragmentReference fragmentReference, ClassLoader classLoader)
            throws RenderableCreationException;

    PageRenderableData createPageRenderable(PageReference pageReference, ClassLoader classLoader)
            throws RenderableCreationException;

    LayoutRenderableData createLayoutRenderable(LayoutReference layoutReference) throws RenderableCreationException;

    int hashCode();

    boolean equals(Object obj);

    class FragmentRenderableData {

        private final Renderable renderable;
        private final Permission permission;

        /**
         * Constructs a fragment renderable data bean.
         *
         * @param renderable renderable
         * @param permission permission for the renderable
         */
        public FragmentRenderableData(Renderable renderable, Permission permission) {
            this.renderable = renderable;
            this.permission = permission;
        }

        public Renderable getRenderable() {
            return renderable;
        }

        /**
         * Returns the permission for the fragment renderable.
         *
         * @return permission for the fragment renderable
         */
        public Permission getPermission() {
            return permission;
        }
    }

    class PageRenderableData {

        private final Renderable renderable;
        private final Permission permission;
        private final String layoutName;

        /**
         * Constructs a page renderable data bean.
         *
         * @param renderable renderable
         * @param permission permission for the renderable
         */
        public PageRenderableData(Renderable renderable, Permission permission) {
            this(renderable, permission, null);
        }

        public PageRenderableData(Renderable renderable, Permission permission, String layoutName) {
            this.renderable = renderable;
            this.permission = permission;
            this.layoutName = layoutName;
        }

        public Renderable getRenderable() {
            return renderable;
        }

        /**
         * Returns the permission for the page renderable.
         *
         * @return permission for the page renderable
         */
        public Permission getPermission() {
            return permission;
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
