/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.uuf.api.config;

import java.util.List;

/**
 * A bean class that represents the component manifest file in an UUF Component.
 *
 * @since 1.0.0
 */
public class ComponentManifest {

    private List<API> apis;
    private List<Binding> bindings;

    /**
     * Returns the APIs entries of this component manifest.
     *
     * @return APIs of this component manifest
     */
    public List<API> getApis() {
        return apis;
    }

    /**
     * Sets the APIs of this component manifest.
     *
     * @param apis APIs to be set
     */
    public void setApis(List<API> apis) {
        this.apis = apis;
    }

    /**
     * Returns the bindings entries of this component manifest.
     *
     * @return bindings of this component manifest
     */
    public List<Binding> getBindings() {
        return bindings;
    }

    /**
     * Sets the bindings of this component manifest.
     *
     * @param bindings bindings to be set
     */
    public void setBindings(List<Binding> bindings) {
        this.bindings = bindings;
    }

    /**
     * Represent an API entry in the component manifest file in an UUF Component.
     *
     * @since 1.0.0
     */
    public static class API {

        private String className;
        private String uri;

        /**
         * Returns the name of the class of this API.
         *
         * @return name of the class of this API
         */
        public String getClassName() {
            return className;
        }

        /**
         * Sets the name of the class of this API.
         *
         * @param className name of the class to be set
         */
        public void setClassName(String className) {
            this.className = className;
        }

        /**
         * Returns the URI of this API.
         *
         * @return URI of this API
         */
        public String getUri() {
            return uri;
        }

        /**
         * Sets the URI of this API.
         *
         * @param uri URI to be set
         */
        public void setUri(String uri) {
            this.uri = uri;
        }
    }

    /**
     * Represent a binding entry in thr component manifest file in an UUF Component.
     *
     * @since 1.0.0
     */
    public static class Binding {

        /**
         * Binding combine mode prepend.
         */
        public static final String MODE_PREPEND = "prepend";
        /**
         * Binging combine mode append.
         */
        public static final String MODE_APPEND = "append";
        /**
         * Binding combine mode overwrite.
         */
        public static final String MODE_OVERWRITE = "overwrite";

        private String zoneName;
        private String mode;
        private List<String> fragments;

        /**
         * Returns the zone name of this binding.
         *
         * @return zone name of this binding
         */
        public String getZoneName() {
            return zoneName;
        }

        /**
         * Sets the zone name of this binding.
         *
         * @param zoneName zone name to be set
         */
        public void setZoneName(String zoneName) {
            this.zoneName = zoneName;
        }

        /**
         * Returns the mode of this binding.
         *
         * @return mode of this binding
         */
        public String getMode() {
            return mode;
        }

        /**
         * Sets the mode of this binding.
         *
         * @param mode mode to be set
         * @exception IllegalArgumentException if the {@code mode} is not {@link #MODE_PREPEND} or {@link #MODE_APPEND}
         *                                     or {@link #MODE_OVERWRITE}
         * @see #MODE_PREPEND
         * @see #MODE_APPEND
         * @see #MODE_OVERWRITE
         */
        public void setMode(String mode) {
            if (MODE_PREPEND.equals(mode) || MODE_APPEND.equals(mode) ||
                    MODE_OVERWRITE.equals(mode)) {
                this.mode = mode;
            } else {
                throw new IllegalArgumentException(
                        "Binding mode should be either '" + MODE_PREPEND + "', '" + MODE_APPEND + "' or '" +
                                MODE_OVERWRITE + "'. Instead found '" + mode + "'.");
            }
        }

        /**
         * Returns the names of the Fragments of this binding.
         *
         * @return fragment names
         */
        public List<String> getFragments() {
            return fragments;
        }

        /**
         * Sets the names of the Fragments of this binding.
         *
         * @param fragments names of the Fragments to be set
         */
        public void setFragments(List<String> fragments) {
            this.fragments = fragments;
        }
    }
}
