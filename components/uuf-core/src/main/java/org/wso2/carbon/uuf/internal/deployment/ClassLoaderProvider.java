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

import java.util.Dictionary;

/**
 * A provider that gives the class loader for a given component.
 *
 * @since 1.0.0
 */
public interface ClassLoaderProvider {

    /**
     * Returns class loader of the given component.
     *
     * @param componentName      full component name
     * @param componentVersion   component version
     * @param componentReference component reference
     * @return class loader for specified component
     */
    ClassLoader getClassLoader(String componentName, String componentVersion, ComponentReference componentReference);

    /**
     * Deploys an API as a MSF4J service.
     *
     * @param serviceImplementation service object which implements the interface
     * @param properties            service specific properties
     */
    void deployAPI(Object serviceImplementation, Dictionary<String, ?> properties);
}
