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

package org.wso2.carbon.uuf.renderablecreator.html.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableSet;
import org.wso2.carbon.uuf.exception.NotSupportedException;
import org.wso2.carbon.uuf.reference.FragmentReference;
import org.wso2.carbon.uuf.reference.LayoutReference;
import org.wso2.carbon.uuf.reference.PageReference;
import org.wso2.carbon.uuf.renderablecreator.html.impl.HtmlRenderable;
import org.wso2.carbon.uuf.spi.Renderable;
import org.wso2.carbon.uuf.spi.RenderableCreator;

import java.util.Set;

@Component(name = "org.wso2.carbon.uuf.renderablecreator.html.internal.HtmlRenderableCreator",
           service = RenderableCreator.class,
           immediate = true)
@SuppressWarnings("unused")
public class HtmlRenderableCreator implements RenderableCreator {

    private static final Set<String> SUPPORTED_FILE_EXTENSIONS = ImmutableSet.of("html");
    private static final Logger log = LoggerFactory.getLogger(HtmlRenderableCreator.class);

    @Activate
    protected void activate() {
        log.debug("HtmlRenderableCreator activated.");
    }

    @Deactivate
    protected void deactivate() {
        log.debug("HtmlRenderableCreator deactivated.");
    }

    @Override
    public Set<String> getSupportedFileExtensions() {
        return SUPPORTED_FILE_EXTENSIONS;
    }

    @Override
    public FragmentRenderableData createFragmentRenderable(FragmentReference fragmentReference,
                                                           ClassLoader classLoader) {
        Renderable renderable = new HtmlRenderable(fragmentReference.getRenderingFile().getContent());
        return new RenderableCreator.FragmentRenderableData(renderable, false);
    }

    @Override
    public PageRenderableData createPageRenderable(PageReference pageReference,
                                                   ClassLoader classLoader) {
        Renderable renderable = new HtmlRenderable(pageReference.getRenderingFile().getContent());
        return new RenderableCreator.PageRenderableData(renderable, false);
    }

    @Override
    public LayoutRenderableData createLayoutRenderable(LayoutReference layoutReference) {
        throw new NotSupportedException("Layouts are not supported for html file content");
    }
}
