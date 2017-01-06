/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uuf.internal.deployment.parser;

import java.util.Collections;
import java.util.List;

/**
 * Bean class that represents the theme's config file of an UUF Theme.
 *
 * Getters and setters of this class should match with getters and setters in
 * org.wso2.carbon.uuf.maven.bean.ThemeConfig class.
 * @since 1.0.0
 */
public class ThemeConfig {

    private List<String> css;
    private List<String> headJs;
    private List<String> js;

    /**
     * Returns the CSS relatives paths of this theme configuration.
     *
     * @return CSS relative paths
     */
    public List<String> getCss() {
        return css;
    }

    /**
     * Sets the CSS relative paths of this theme configuration.
     *
     * @param css CSS relative paths to be set
     */
    public void setCss(List<String> css) {
        this.css = (css == null) ? Collections.emptyList() : css;
    }

    /**
     * Returns the header JS relatives paths of this theme configuration.
     *
     * @return header JS relative paths
     */
    public List<String> getHeadJs() {
        return headJs;
    }

    /**
     * Sets the head JS relative paths of this theme configuration.
     *
     * @param headJs head JS relative paths to be set
     */
    public void setHeadJs(List<String> headJs) {
        this.headJs = (headJs == null) ? Collections.emptyList() : headJs;
    }

    /**
     * Returns the footer JS relatives paths of this theme configuration.
     *
     * @return footer JS relative paths
     */
    public List<String> getJs() {
        return js;
    }

    /**
     * Sets the footer JS relative paths of this theme configuration.
     *
     * @param js footer JS relative paths to be set
     */
    public void setJs(List<String> js) {
        this.js = (js == null) ? Collections.emptyList() : js;
    }
}
