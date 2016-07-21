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

package org.wso2.carbon.uuf.api;

/**
 * Holds the constants for allowed placeholders for the layouts.
 */
public enum Placeholder {

    // These enums are named in camelcase because end-developer uses/access placeholders in same string.
    /**
     * Placeholder for the favicon which will be inside the {@code <head>} tag.
     */
    favicon,
    /**
     * Placeholder for the title of the page which will be inside the {@code <head>} tag.
     */
    title,
    /**
     * Placeholder for CSS style-sheet link which will be inside the {@code <head>} tag.
     */
    css,
    /**
     * Placeholder for JavaScript {@code <script>} tags that are in the {@code <head>} tag of the page.
     */
    headJs,
    /**
     * Placeholder for other tags that will go inside the {@code <head>} tag. e.g. {@code <meta>}
     */
    headOther,
    /**
     * Placeholder for JavScript {@code <script>} tags that will be at the bottom of the {@code <body>} tag.
     */
    js
}
