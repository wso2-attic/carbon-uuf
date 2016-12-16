package org.wso2.carbon.uuf;

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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.api.reference.AppReference;
import org.wso2.carbon.uuf.api.reference.ComponentReference;
import org.wso2.carbon.uuf.api.reference.FileReference;
import org.wso2.carbon.uuf.internal.deployment.AppCreator;
import org.wso2.carbon.uuf.internal.deployment.ClassLoaderProvider;
import org.wso2.carbon.uuf.spi.RenderableCreator;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * This test class contains test method(s) to test the API deployment capability feature of UUF.
 */
public class APITest {

    @SuppressWarnings({"unchecked"})
    @Test
    public void testAPIContextPath() throws ClassNotFoundException {
        String appContextpath = "/sample-app";
        String apiClassName = "org.wso2.carbon.uuf.SampleAPI";
        String apiURI = "/sample-api";

        RenderableCreator renderableCreator = mock(RenderableCreator.class);
        when(renderableCreator.getSupportedFileExtensions()).thenReturn(new HashSet());
        Set<RenderableCreator> renderableCreators = new HashSet<>();
        renderableCreators.add(renderableCreator);

        FileReference dependencyTreeFileReference = mock(FileReference.class);
        FileReference configFileReference = mock(FileReference.class);
        FileReference manifestFileReference = mock(FileReference.class);
        when(dependencyTreeFileReference.getContent()).thenReturn(
                "artifactId: org.wso2.carbon.uuf.sample.sample-app.feature\n" +
                        "version: 1.0.0-SNAPSHOT\n" +
                        "contextPath: " + appContextpath);
        when(manifestFileReference.getContent()).thenReturn(
                "apis:\n" +
                        "    - className: \"" + apiClassName + "\"\n" +
                        "      uri: \"" + apiURI + "\"");
        when(configFileReference.getContent()).thenReturn("appName: Sample Application");

        ComponentReference componentReference = mock(ComponentReference.class);
        when(componentReference.getLayouts(any())).thenReturn(Stream.empty());
        when(componentReference.getFragments(any())).thenReturn(Stream.empty());
        when(componentReference.getPages(any())).thenReturn(Stream.empty());
        when(componentReference.getManifest()).thenReturn(Optional.of(manifestFileReference));

        AppReference appReference = mock(AppReference.class);
        when(appReference.getConfiguration()).thenReturn(configFileReference);
        when(appReference.getDependencyTree()).thenReturn(dependencyTreeFileReference);
        when(appReference.getThemeReferences()).thenReturn(Stream.empty());
        when(appReference.getComponentReference(any())).thenReturn(componentReference);

        ClassLoaderProvider classLoaderProvider = mock(ClassLoaderProvider.class);
        ClassLoader classLoader = mock(ClassLoader.class);
        when(classLoaderProvider.getClassLoader(any(), any(), any())).thenReturn(classLoader);
        when(classLoader.loadClass(apiClassName)).thenReturn((Class) SampleAPI.class);

        // This single element array is used to store the api context path
        final String[] apiContextPath = new String[1];
        doAnswer(invocation -> {
            Dictionary<String, String> serviceProperties = (Dictionary<String, String>) invocation.getArguments()[1];
            apiContextPath[0] = serviceProperties.get("contextPath");
            return null;
        }).when(classLoaderProvider).deployAPI(any(), any());

        AppCreator appCreator = new AppCreator(renderableCreators, classLoaderProvider);
        appCreator.createApp(appReference, appContextpath);

        String expectedApiContextPath = appContextpath + "/root/apis" + apiURI;
        Assert.assertEquals(apiContextPath[0], expectedApiContextPath, "Calculated API context path is wrong.");
    }

}
