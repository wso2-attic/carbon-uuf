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

import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.renderablecreator.hbs.core.MutableExecutable;

public class MutableJsExecutable implements MutableExecutable {

    private final ClassLoader componentClassLoader;
    private final String absolutePath;
    private final String relativePath;
    private final String componentPath;

    private JsExecutable jsExecutable;

    public MutableJsExecutable(String scriptSource, ClassLoader componentClassLoader, String absolutePath,
                               String relativePath, String componentPath) {
        this.componentClassLoader = componentClassLoader;
        this.absolutePath = absolutePath;
        this.relativePath = relativePath;
        this.componentPath = componentPath;

        this.jsExecutable = new JsExecutable(scriptSource, componentClassLoader, absolutePath, relativePath, componentPath);
    }

    @Override
    public Object execute(Object context, API api, Lookup lookup, RequestLookup requestLookup) {
        return jsExecutable.execute(context, api, lookup, requestLookup);
    }

    @Override
    public String getPath() {
        return jsExecutable.getAbsolutePath();
    }

    @Override
    public void reload(String scriptSource) {
        this.jsExecutable = new JsExecutable(scriptSource, componentClassLoader, absolutePath, relativePath, componentPath);
    }
}
