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

public class Bindings {

    private final ListMultimap<String, Fragment> bindings;

    public Bindings() {
        this.bindings = ArrayListMultimap.create();
    }

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

    public List<Fragment> getBinding(String zoneName) {
        return bindings.get(zoneName);
    }

    public enum Mode {
        prepend, append, overwrite
    }
}
