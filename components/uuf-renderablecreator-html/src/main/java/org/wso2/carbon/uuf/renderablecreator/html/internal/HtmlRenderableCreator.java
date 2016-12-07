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

import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.reference.FileReference;
import org.wso2.carbon.uuf.api.reference.FragmentReference;
import org.wso2.carbon.uuf.api.reference.LayoutReference;
import org.wso2.carbon.uuf.api.reference.PageReference;
import org.wso2.carbon.uuf.renderablecreator.html.core.HtmlRenderable;
import org.wso2.carbon.uuf.renderablecreator.html.core.MutableHtmlRenderable;
import org.wso2.carbon.uuf.renderablecreator.html.internal.io.HtmlRenderableUpdater;
import org.wso2.carbon.uuf.spi.Renderable;
import org.wso2.carbon.uuf.spi.RenderableCreator;

import java.util.Set;

@Component(name = "org.wso2.carbon.uuf.renderablecreator.html.internal.HtmlRenderableCreator",
           service = RenderableCreator.class,
           immediate = true)
@SuppressWarnings("unused")
public class HtmlRenderableCreator implements RenderableCreator {

    private static final Set<String> SUPPORTED_FILE_EXTENSIONS = ImmutableSet.of("html");
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlRenderableCreator.class);

    private final boolean isDevmodeEnabled;
    private final HtmlRenderableUpdater updater;

    public HtmlRenderableCreator() {
        this.isDevmodeEnabled = Boolean.parseBoolean(System.getProperties().getProperty("devmode", "false"));
        if (this.isDevmodeEnabled) {
            updater = new HtmlRenderableUpdater();
        } else {
            updater = null;
        }
    }

    @Activate
    protected void activate() {
        LOGGER.debug("{} activated.", getClass().getName());
        if (isDevmodeEnabled) {
            updater.start();
        }
    }

    @Deactivate
    protected void deactivate() {
        LOGGER.debug("{} deactivated.", getClass().getName());
        if (isDevmodeEnabled) {
            updater.finish();
        }
    }

    @Override
    public Set<String> getSupportedFileExtensions() {
        return SUPPORTED_FILE_EXTENSIONS;
    }

    @Override
    public FragmentRenderableData createFragmentRenderable(FragmentReference fragmentReference,
                                                           ClassLoader classLoader) {
        return new RenderableCreator.FragmentRenderableData(getHtmlRenderable(fragmentReference.getRenderingFile()),
                                                            false);
    }

    @Override
    public PageRenderableData createPageRenderable(PageReference pageReference,
                                                   ClassLoader classLoader) {
        return new RenderableCreator.PageRenderableData(getHtmlRenderable(pageReference.getRenderingFile()), false);
    }

    @Override
    public LayoutRenderableData createLayoutRenderable(LayoutReference layoutReference) {
        throw new UnsupportedOperationException("Layouts are not supported for html file content");
    }

    private Renderable getHtmlRenderable(FileReference fileReference) {

        if (isDevmodeEnabled) {
            MutableHtmlRenderable mutableHtmlRenderable = new MutableHtmlRenderable(fileReference.getContent(),
                                                                                    fileReference.getAbsolutePath(),
                                                                                    fileReference.getRelativePath());
            updater.add(mutableHtmlRenderable);
            return mutableHtmlRenderable;
        } else {
            return new HtmlRenderable(fileReference.getContent(), fileReference.getAbsolutePath(),
                                      fileReference.getRelativePath());
        }
    }
}
