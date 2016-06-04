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

package org.wso2.carbon.uuf.handlebars;

import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.uuf.handlebars.renderable.HbsFragmentRenderable;
import org.wso2.carbon.uuf.handlebars.renderable.HbsLayoutRenderable;
import org.wso2.carbon.uuf.handlebars.renderable.HbsPageRenderable;
import org.wso2.carbon.uuf.reference.FileReference;
import org.wso2.carbon.uuf.reference.FragmentReference;
import org.wso2.carbon.uuf.reference.LayoutReference;
import org.wso2.carbon.uuf.reference.PageReference;
import org.wso2.carbon.uuf.spi.RenderableCreator;

import java.util.Set;

@Component(name = "org.wso2.carbon.uuf.handlebars.HbsRenderableCreator",
           service = RenderableCreator.class,
           immediate = true)
public class HbsRenderableCreator implements RenderableCreator {

    private static final String EXTENSION_HANDLEBARS = ".hbs";
    private static final String EXTENSION_JAVASCRIPT = ".js";
    private static final Set<String> SUPPORTED_FILE_EXTENSIONS = ImmutableSet.of("hbs");

    @Override
    public Set<String> getSupportedFileExtensions() {
        return SUPPORTED_FILE_EXTENSIONS;
    }

    @Override
    public FragmentRenderableData createFragmentRenderable(FragmentReference fragmentReference,
                                                           ClassLoader classLoader) {
        TemplateSource templateSource = createTemplateSource(fragmentReference.getRenderingFile());
        Executable executable = createSameNameJs(fragmentReference.getRenderingFile(), classLoader);
        HbsFragmentRenderable fragmentRenderable = new HbsFragmentRenderable(templateSource, executable);
        boolean isSecured = new HbsPreprocessor(templateSource).isSecured();
        return new RenderableCreator.FragmentRenderableData(fragmentRenderable, isSecured);
    }

    @Override
    public PageRenderableData createPageRenderable(PageReference pageReference,
                                                   ClassLoader classLoader) {
        TemplateSource templateSource = createTemplateSource(pageReference.getRenderingFile());
        Executable executable = createSameNameJs(pageReference.getRenderingFile(), classLoader);
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

    private Executable createSameNameJs(FileReference pageReference, ClassLoader classLoader) {
        String jsName = withoutExtension(pageReference.getName()) + EXTENSION_JAVASCRIPT;
        return pageReference.getSibling(jsName)
                .map(fr -> new JSExecutable(fr.getContent(), fr.getAbsolutePath(), classLoader))
                .orElse(null);
    }

    private String withoutExtension(String name) {
        return name.substring(0, (name.length() - EXTENSION_HANDLEBARS.length()));
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
