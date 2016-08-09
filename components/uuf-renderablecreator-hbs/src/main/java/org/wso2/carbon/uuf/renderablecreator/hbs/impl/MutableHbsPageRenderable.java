/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.uuf.renderablecreator.hbs.impl;

import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateSource;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.Executable;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.MutableExecutable;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.MutableHbsRenderable;

import java.util.Optional;

public class MutableHbsPageRenderable extends HbsPageRenderable implements MutableHbsRenderable {

    private volatile Template template;
    private final MutableExecutable mutableExecutable;

    public MutableHbsPageRenderable(TemplateSource templateSource, String absolutePath, String relativePath,
                                    MutableExecutable mutableExecutable) {
        super(null, absolutePath, relativePath, null);
        this.template = compile(templateSource);
        this.mutableExecutable = mutableExecutable;
    }

    @Override
    public String getPath() {
        return getAbsolutePath();
    }

    @Override
    protected Template getTemplate() {
        return template;
    }

    @Override
    protected Executable getExecutable() {
        return mutableExecutable;
    }

    @Override
    public void reload(TemplateSource templateSource) {
        template = compile(templateSource);
    }

    @Override
    public Optional<MutableExecutable> getMutableExecutable() {
        return Optional.ofNullable(mutableExecutable);
    }
}
