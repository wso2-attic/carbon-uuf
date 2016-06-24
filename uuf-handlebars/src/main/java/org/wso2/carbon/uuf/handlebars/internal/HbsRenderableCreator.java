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

package org.wso2.carbon.uuf.handlebars.internal;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FilenameUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.handlebars.renderable.Executable;
import org.wso2.carbon.uuf.handlebars.renderable.HbsFragmentRenderable;
import org.wso2.carbon.uuf.handlebars.renderable.HbsLayoutRenderable;
import org.wso2.carbon.uuf.handlebars.renderable.HbsPageRenderable;
import org.wso2.carbon.uuf.handlebars.renderable.js.JSExecutable;
import org.wso2.carbon.uuf.reference.FileReference;
import org.wso2.carbon.uuf.reference.FragmentReference;
import org.wso2.carbon.uuf.reference.LayoutReference;
import org.wso2.carbon.uuf.reference.PageReference;
import org.wso2.carbon.uuf.spi.RenderableCreator;

import java.util.Optional;
import java.util.Set;

@Component(name = "org.wso2.carbon.uuf.handlebars.internal.HbsRenderableCreator",
           service = RenderableCreator.class,
           immediate = true)
public class HbsRenderableCreator implements RenderableCreator {

    private static final String EXTENSION_HANDLEBARS = ".hbs";
    private static final String EXTENSION_JAVASCRIPT = ".js";
    private static final Set<String> SUPPORTED_FILE_EXTENSIONS = ImmutableSet.of("hbs");
    private static final Logger log = LoggerFactory.getLogger(HbsRenderableCreator.class);

    @Activate
    protected void activate() {
        log.debug("HbsRenderableCreator activated.");
    }

    @Deactivate
    protected void deactivate() {
        log.debug("HbsRenderableCreator deactivated.");
    }

    @Override
    public Set<String> getSupportedFileExtensions() {
        return SUPPORTED_FILE_EXTENSIONS;
    }

    @Override
    public FragmentRenderableData createFragmentRenderable(FragmentReference fragmentReference,
                                                           ClassLoader classLoader) {
        TemplateSource templateSource = createTemplateSource(fragmentReference.getRenderingFile());
        Executable executable = createExecutable(fragmentReference, classLoader);
        HbsFragmentRenderable fragmentRenderable = new HbsFragmentRenderable(templateSource, executable);
        boolean isSecured = new HbsPreprocessor(templateSource).isSecured();
        return new RenderableCreator.FragmentRenderableData(fragmentRenderable, isSecured);
    }

    @Override
    public PageRenderableData createPageRenderable(PageReference pageReference,
                                                   ClassLoader classLoader) {
        TemplateSource templateSource = createTemplateSource(pageReference.getRenderingFile());
        Executable executable = createExecutable(pageReference, classLoader);
        HbsPageRenderable pageRenderable = new HbsPageRenderable(templateSource, executable);
        HbsPreprocessor preprocessor = new HbsPreprocessor(templateSource);
        String layoutName = preprocessor.getLayoutName().orElse(null);
        return new RenderableCreator.PageRenderableData(pageRenderable, preprocessor.isSecured(), layoutName);
    }

    @Override
    public LayoutRenderableData createLayoutRenderable(LayoutReference layoutReference) {
        TemplateSource templateSource = createTemplateSource(layoutReference.getRenderingFile());
        return new RenderableCreator.LayoutRenderableData(new HbsLayoutRenderable(templateSource));
    }

    private TemplateSource createTemplateSource(FileReference pageReference) {
        return new StringTemplateSource(pageReference.getRelativePath(), pageReference.getContent());
    }

    private Executable createExecutable(FragmentReference fragmentReference, ClassLoader classLoader) {
        return getExecutableFile(fragmentReference.getRenderingFile())
                .map(efr -> new JSExecutable(efr.getContent(), classLoader, efr.getAbsolutePath(),
                                             fragmentReference.getComponentReference().getPath()))
                .orElse(null);
    }

    private Executable createExecutable(PageReference pageReference, ClassLoader classLoader) {
        return getExecutableFile(pageReference.getRenderingFile())
                .map(efr -> new JSExecutable(efr.getContent(), classLoader, efr.getAbsolutePath(),
                                             pageReference.getComponentReference().getPath()))
                .orElse(null);
    }

    private Optional<FileReference> getExecutableFile(FileReference renderableFileReference) {
        String jsFileName = FilenameUtils.removeExtension(renderableFileReference.getName()) + EXTENSION_JAVASCRIPT;
        return renderableFileReference.getSibling(jsFileName);
    }

    @Override
    public int hashCode() {
        return getSupportedFileExtensions().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof RenderableCreator) &&
                this.getSupportedFileExtensions().equals(((RenderableCreator) obj).getSupportedFileExtensions());
    }
}
