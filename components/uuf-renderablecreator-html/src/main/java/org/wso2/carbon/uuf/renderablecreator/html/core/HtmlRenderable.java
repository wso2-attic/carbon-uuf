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

package org.wso2.carbon.uuf.renderablecreator.html.core;

import org.wso2.carbon.uuf.core.API;
import org.wso2.carbon.uuf.core.Lookup;
import org.wso2.carbon.uuf.core.RequestLookup;
import org.wso2.carbon.uuf.spi.Renderable;
import org.wso2.carbon.uuf.spi.model.Model;

import java.util.Objects;

public class HtmlRenderable implements Renderable {

    private final String html;
    private final String absoluteFilePath;
    private final String relativeFilePath;

    public HtmlRenderable(String html) {
        this(html, null, null);
    }

    public HtmlRenderable(String html, String absoluteFilePath, String relativeFilePath) {
        this.absoluteFilePath = absoluteFilePath;
        this.relativeFilePath = relativeFilePath;
        this.html = html;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    @Override
    public String render(Model model, Lookup lookup, RequestLookup requestLookup, API api) {
        return html;
    }

    @Override
    public int hashCode() {
        return Objects.hash(absoluteFilePath, html);
    }

    @Override
    public String toString() {
        return "{\"path\": {\"absolute\": \"" + absoluteFilePath + "\", \"relative\": \"" + relativeFilePath +
                "\"}}";
    }
}
