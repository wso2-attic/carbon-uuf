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

package org.wso2.carbon.uuf.internal.deployment;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.reference.AppReference;
import org.wso2.carbon.uuf.api.reference.ComponentReference;
import org.wso2.carbon.uuf.api.reference.FileReference;

import java.util.Collections;
import java.util.Dictionary;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This test class contains test method(s) to test the API deployment capability feature of UUF.
 */
public class RestApiDeploymentTest {

    @Test
    public void testAPIContextPath() throws ClassNotFoundException {
        String appContextPath = "/sample-app";
        String apiClassName = "org.wso2.carbon.uuf.SampleAPI";
        String apiUri = "/sample-api";

        // Create root component reference.
        ComponentReference rootComponentReference = mock(ComponentReference.class);
        when(rootComponentReference.getLayouts(any())).thenReturn(Stream.empty());
        when(rootComponentReference.getFragments(any())).thenReturn(Stream.empty());
        when(rootComponentReference.getPages(any())).thenReturn(Stream.empty());
        FileReference componentConfigurationFileReference = mock(FileReference.class);
        when(componentConfigurationFileReference.getContent())
                .thenReturn("# testing\n" +
                                    "apis:\n" +
                                    "  - className: " + apiClassName + "\n" +
                                    "    uri: " + apiUri + "\n" +
                                    "bindings: null\n" +
                                    "config: null");
        when(rootComponentReference.getConfiguration()).thenReturn(componentConfigurationFileReference);

        // Create app reference.
        AppReference appReference = mock(AppReference.class);
        when(appReference.getThemeReferences()).thenReturn(Stream.empty());
        when(appReference.getComponentReference(eq("/root"))).thenReturn(rootComponentReference);
        FileReference appConfigurationFileReference = mock(FileReference.class);
        when(appConfigurationFileReference.getContent())
                .thenReturn("# testing\n" +
                                    "contextPath: null\n" +
                                    "theme: null\n" +
                                    "errorPages: null\n" +
                                    "menus: null\n" +
                                    "other: null\n" +
                                    "security: null");
        when(appReference.getConfiguration()).thenReturn(appConfigurationFileReference);
        FileReference dependencyTreeFileReference = mock(FileReference.class);
        when(dependencyTreeFileReference.getContent())
                .thenReturn("# testing\n" +
                                    "artifactId: org.wso2.carbon.uuf.sample.sample-app.feature\n" +
                                    "version: 1.0.0-SNAPSHOT\n" +
                                    "contextPath: " + appContextPath);
        when(appReference.getDependencyTree()).thenReturn(dependencyTreeFileReference);

        // Create class loader provider.
        ClassLoaderProvider classLoaderProvider = mock(ClassLoaderProvider.class);
        when(classLoaderProvider.getClassLoader(any(), any(), any())).thenReturn(ClassLoader.getSystemClassLoader());
        String[] apiContextPath = new String[1]; // variable used in a lambda should be final or effectively final
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Dictionary<String, String> serviceProperties = (Dictionary<String, String>) invocation.getArguments()[1];
            apiContextPath[0] = serviceProperties.get("contextPath");
            return null;
        }).when(classLoaderProvider).deployAPI(any(), any());

        // Create app creator.
        AppCreator appCreator = new AppCreator(Collections.emptySet(), classLoaderProvider);
        appCreator.createApp(appReference, appContextPath);

        String expectedApiContextPath = appContextPath + "/root/apis" + apiUri;
        Assert.assertEquals(apiContextPath[0], expectedApiContextPath, "Calculated API context path is wrong.");
    }

}
