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

package org.wso2.carbon.uuf.internal.deployment;

import org.wso2.carbon.uuf.api.reference.ComponentReference;

/**
 * A provider that gives class loaders for UUF components.
 *
 * @since 1.0.0
 */
public interface ClassLoaderProvider {

    /**
     * Returns a class loader of the specified UUF component.
     *
     * @param componentName      fully qualified name of the component
     * @param componentVersion   version of the component
     * @param componentReference reference to the component
     * @return class loader for the specified component
     */
    ClassLoader getClassLoader(String componentName, String componentVersion, ComponentReference componentReference);
}
