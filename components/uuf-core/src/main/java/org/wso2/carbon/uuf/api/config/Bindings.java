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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.wso2.carbon.uuf.core.Fragment;
import org.wso2.carbon.uuf.internal.util.NameUtils;

import java.util.List;

/**
 * Represents the final bindings of an UUF app.
 *
 * @since 1.0.0
 */
public class Bindings {

    private final ListMultimap<String, Fragment> bindings;

    /**
     * Creates a new instance.
     */
    public Bindings() {
        this.bindings = ArrayListMultimap.create();
    }

    /**
     * Adds a new binding entry.
     *
     * @param zoneName  zone name that the specified fragments will be bound
     * @param fragments fragments that will be bound
     * @param mode      binding mode
     * @throws IllegalArgumentException if zone name is either null, empty or not a fully qualified name
     * @throws IllegalArgumentException if specified fragment/s is/are null
     * @throws IllegalArgumentException if mode is null
     */
    public void addBinding(String zoneName, List<Fragment> fragments, Mode mode) {
        // Validating zone name.
        if (zoneName == null) {
            throw new IllegalArgumentException("Zone name of a binding cannot be null.");
        } else if (zoneName.isEmpty()) {
            throw new IllegalArgumentException("Zone name of a binding cannot be empty.");
        } else if (!NameUtils.isFullyQualifiedName(zoneName)) {
            throw new IllegalArgumentException(
                    "Zone name of a binding should be a fully qualified name. Instead found '" + zoneName + "'.");
        }

        // Validating fragments list.
        if (fragments == null) {
            throw new IllegalArgumentException("Fragments of a binding cannot be null.");
        } else {
            for (Fragment fragment : fragments) {
                if (fragment == null) {
                    throw new IllegalArgumentException("Fragments in the fragments list in a binding cannot be null.");
                }
            }
        }

        // Validating mode.
        if (mode == null) {
            throw new IllegalArgumentException("Mode of a binding cannot be null.");
        }

        switch (mode) {
            case prepend:
                bindings.get(zoneName).addAll(0, fragments);
                break;
            case append:
                bindings.putAll(zoneName, fragments);
                break;
            case overwrite:
                bindings.replaceValues(zoneName, fragments);
                break;
        }
    }

    /**
     * Returns the bound fragments of the specified zone.
     *
     * @param zoneName fully qualified name of the zone
     * @return bound fragments to the specified zone
     */
    public List<Fragment> getBindings(String zoneName) {
        return bindings.get(zoneName);
    }

    /**
     * Represents different pushing modes for a binding entry.
     */
    public enum Mode {
        /**
         * Pushes the specified fragments into the beginning of the existing pushed fragments of the zone.
         */
        prepend,
        /**
         * Pushes the specified fragments into the end of the existing pushed fragments of the zone.
         */
        append,
        /**
         * Removes the existing pushed fragments of the zone and replace with specified fragments.
         */
        overwrite
    }
}
