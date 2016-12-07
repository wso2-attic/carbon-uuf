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

package org.wso2.carbon.uuf.renderablecreator.hbs.internal;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FilenameUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.api.reference.ComponentReference;
import org.wso2.carbon.uuf.api.reference.FileReference;
import org.wso2.carbon.uuf.api.reference.FragmentReference;
import org.wso2.carbon.uuf.api.reference.LayoutReference;
import org.wso2.carbon.uuf.api.reference.PageReference;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.Executable;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.MutableExecutable;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.HbsFragmentRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.HbsLayoutRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.HbsPageRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.JsExecutable;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.MutableHbsFragmentRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.MutableHbsLayoutRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.MutableHbsPageRenderable;
import org.wso2.carbon.uuf.renderablecreator.hbs.impl.MutableJsExecutable;
import org.wso2.carbon.uuf.renderablecreator.hbs.internal.io.HbsRenderableUpdater;
import org.wso2.carbon.uuf.spi.Renderable;
import org.wso2.carbon.uuf.spi.RenderableCreator;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component(name = "org.wso2.carbon.uuf.renderablecreator.hbs.internal.HbsRenderableCreator",
           service = RenderableCreator.class,
           immediate = true)
@SuppressWarnings("unused")
public class HbsRenderableCreator implements RenderableCreator {

    private static final Set<String> SUPPORTED_FILE_EXTENSIONS = ImmutableSet.of("hbs");
    private static final String EXTENSION_JAVASCRIPT = ".js";
    private static final Logger LOGGER = LoggerFactory.getLogger(HbsRenderableCreator.class);

    private final boolean isDevmodeEnabled;
    private final HbsRenderableUpdater updater;

    public HbsRenderableCreator() {
        this.isDevmodeEnabled = Boolean.parseBoolean(System.getProperties().getProperty("devmode", "false"));
        if (this.isDevmodeEnabled) {
            updater = new HbsRenderableUpdater();
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
        FileReference file = fragmentReference.getRenderingFile();
        TemplateSource templateSource = createTemplateSource(file);
        Executable executable = createExecutable(fragmentReference, classLoader);
        Renderable fragmentRenderable;
        if (isDevmodeEnabled) {
            MutableHbsFragmentRenderable mfr = new MutableHbsFragmentRenderable(templateSource,
                                                                                file.getAbsolutePath(),
                                                                                file.getRelativePath(),
                                                                                (MutableExecutable) executable);
            fragmentRenderable = mfr;
            updater.add(fragmentReference, mfr);
        } else {
            fragmentRenderable = new HbsFragmentRenderable(templateSource, file.getAbsolutePath(),
                                                           file.getRelativePath(), executable);
        }
        boolean isSecured = new HbsPreprocessor(templateSource).isSecured();
        return new RenderableCreator.FragmentRenderableData(fragmentRenderable, isSecured);
    }

    @Override
    public PageRenderableData createPageRenderable(PageReference pageReference, ClassLoader classLoader) {
        FileReference file = pageReference.getRenderingFile();
        TemplateSource templateSource = createTemplateSource(file);
        Executable executable = createExecutable(pageReference, classLoader);
        Renderable pageRenderable;
        if (isDevmodeEnabled) {
            MutableHbsPageRenderable mpr = new MutableHbsPageRenderable(templateSource, file.getAbsolutePath(),
                                                                        file.getRelativePath(),
                                                                        (MutableExecutable) executable);
            pageRenderable = mpr;
            updater.add(pageReference, mpr);
        } else {
            pageRenderable = new HbsPageRenderable(templateSource, file.getAbsolutePath(), file.getRelativePath(),
                                                   executable);
        }
        HbsPreprocessor preprocessor = new HbsPreprocessor(templateSource);
        String layoutName = preprocessor.getLayoutName().orElse(null);
        return new RenderableCreator.PageRenderableData(pageRenderable, preprocessor.isSecured(), layoutName);
    }

    @Override
    public LayoutRenderableData createLayoutRenderable(LayoutReference layoutReference) {
        FileReference file = layoutReference.getRenderingFile();
        TemplateSource templateSource = createTemplateSource(file);
        Renderable layoutRenderable;
        if (isDevmodeEnabled) {
            MutableHbsLayoutRenderable mlr = new MutableHbsLayoutRenderable(templateSource, file.getAbsolutePath(),
                                                                            file.getRelativePath());
            updater.add(layoutReference, mlr);
            layoutRenderable = mlr;
        } else {
            layoutRenderable = new HbsLayoutRenderable(templateSource, file.getAbsolutePath(), file.getRelativePath());
        }
        return new RenderableCreator.LayoutRenderableData(layoutRenderable);
    }

    private TemplateSource createTemplateSource(FileReference fileReference) {
        return new StringTemplateSource(fileReference.getRelativePath(), fileReference.getContent());
    }

    private Executable createExecutable(FragmentReference fragmentReference, ClassLoader classLoader) {
        return getExecutableFile(fragmentReference.getRenderingFile())
                .map(efr -> createExecutable(efr, classLoader, fragmentReference.getComponentReference()))
                .orElse(null);
    }

    private Executable createExecutable(PageReference pageReference, ClassLoader classLoader) {
        return getExecutableFile(pageReference.getRenderingFile())
                .map(efr -> createExecutable(efr, classLoader, pageReference.getComponentReference()))
                .orElse(null);
    }

    private Optional<FileReference> getExecutableFile(FileReference renderableFileReference) {
        String jsFileName = FilenameUtils.removeExtension(renderableFileReference.getName()) + EXTENSION_JAVASCRIPT;
        return renderableFileReference.getSibling(jsFileName);
    }

    private Executable createExecutable(FileReference executableFileReference, ClassLoader classLoader,
                                        ComponentReference componentReference) {
        if (isDevmodeEnabled) {
            return new MutableJsExecutable(executableFileReference.getContent(), classLoader,
                                           executableFileReference.getAbsolutePath(),
                                           executableFileReference.getRelativePath(), componentReference.getPath());
        } else {
            return new JsExecutable(executableFileReference.getContent(), classLoader,
                                    executableFileReference.getAbsolutePath(),
                                    executableFileReference.getRelativePath(), componentReference.getPath());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSupportedFileExtensions().hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof RenderableCreator) &&
                this.getSupportedFileExtensions().equals(((RenderableCreator) obj).getSupportedFileExtensions());
    }
}
