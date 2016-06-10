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

package org.wso2.carbon.uuf.internal.io;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;
import org.wso2.carbon.uuf.exception.UUFException;
import org.wso2.carbon.uuf.internal.core.create.ClassLoaderProvider;
import org.wso2.carbon.uuf.reference.ComponentReference;
import org.wso2.carbon.uuf.reference.FileReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class BundleClassLoaderProvider implements ClassLoaderProvider {

    private static final String DUMMY_CLASS_NAME = "DummyComponentBundle.class";
    private static byte[] dummyBundleClassByteCodes;

    static {
        try {
            dummyBundleClassByteCodes = DummyBundleClass.dump();
        } catch (Exception e) {
            throw new UUFException("Cannot create the dummy class for OSGi bundle creation.");
        }
    }

    @Override
    public ClassLoader getClassLoader(String componentName, String componentVersion,
                                      ComponentReference componentReference) {
        Bundle bundle = getBundle(componentName, componentVersion, componentReference);
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        return bundleWiring.getClassLoader();
    }

    /**
     * If no bundle exists for provided component reference, It create and returns a new OSGi bundle. Or else it will
     * return the existing bundle. Created bundle is reusable across multiple UUF Apps.
     *
     * @return created OSGi bundle
     */
    private Bundle getBundle(String componentName, String componentVersion, ComponentReference componentReference) {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        Bundle bundle = bundleContext.getBundle(componentName);
        if (bundle != null) {
            return bundle;
        }

        try {
            return createBundle(componentName, getBundleVersion(componentVersion), getImports(componentReference));
        } catch (IOException e) {
            throw new UUFException("Error while creating the OSGi bundle for component '" + componentName + "-" +
                                           componentVersion + "'.", e);
        } catch (BundleException e) {
            throw new UUFException("Error while installing the OSGi bundle of component '" + componentName + "-" +
                                           componentVersion + "'.", e);
        }
    }

    private String getBundleVersion(String componentVersion) {
        return componentVersion.replace('-', '.').replace('_', '.'); // There shouldn't be any '-' or '_' in version
    }

    private List<String> getImports(ComponentReference componentReference) {
        Optional<FileReference> osgiImportsConfig = componentReference.getOsgiImportsConfig();
        if (!osgiImportsConfig.isPresent()) {
            return Collections.<String>emptyList();
        }
        String[] lines = osgiImportsConfig.get().getContent().split("\\r?\\n");
        List<String> rv = new ArrayList<>(lines.length);
        for (String line : lines) {
            if (!line.isEmpty() && !line.startsWith("#")) {
                // Skip empty lines & comment lines.
                rv.add(line);
            }
        }
        return rv;
    }

    private Bundle createBundle(String bundleKey, String bundleVersion, List<String> imports)
            throws IOException, BundleException {

        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        Bundle bundle = bundleContext.getBundle(bundleKey);
        if (bundle != null) {
            return bundle;
        }

        String bundleName = "UUF bundle for " + bundleKey;
        InputStream bundleInputStream = createBundleStream(bundleName, bundleKey, bundleVersion, imports);
        bundle = bundleContext.installBundle(bundleKey, bundleInputStream);
        bundle.start();
        return bundle;
    }

    private InputStream createBundleStream(String name, String symbolicName, String version, List<String> imports)
            throws IOException {
        Manifest bundleManifest = new Manifest();
        Attributes attributes = bundleManifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.putValue("Bundle-ManifestVersion", "2");
        attributes.putValue("Bundle-Name", name);
        attributes.putValue("Bundle-SymbolicName", symbolicName);
        attributes.putValue("Bundle-Version", version);

        if (!imports.isEmpty()) {
            attributes.putValue("Import-Package", String.join(",", imports));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JarOutputStream target = new JarOutputStream(outputStream, bundleManifest)) {
            //you need at least one java class file for osgi bundle
            addJarEntry(DUMMY_CLASS_NAME, dummyBundleClassByteCodes, target);
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private void addJarEntry(String fileName, byte[] content, JarOutputStream target) throws IOException {
        JarEntry entry = new JarEntry(fileName.replace("\\", "/"));
        target.putNextEntry(entry);
        target.write(content);
        target.closeEntry();
    }

    /**
     * This class intends to use as dummy class for the OSGi bundle. Auto-generated using ASM plugin.
     */
    private static class DummyBundleClass implements Opcodes {

        public static byte[] dump() throws Exception {

            ClassWriter cw = new ClassWriter(0);
            MethodVisitor mv;

            cw.visit(52, ACC_PUBLIC + ACC_SUPER, "DummyBundleClass", null, "java/lang/Object", null);

            cw.visitSource("DummyBundleClass.java", null);

            {
                mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
                mv.visitCode();
                Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitLineNumber(1, l0);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                mv.visitInsn(RETURN);
                Label l1 = new Label();
                mv.visitLabel(l1);
                mv.visitLocalVariable("this", "LDummyBundleClass;", null, l0, l1, 0);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            {
                mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
                mv.visitCode();
                Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitLineNumber(4, l0);
                mv.visitInsn(RETURN);
                Label l1 = new Label();
                mv.visitLabel(l1);
                mv.visitLocalVariable("args", "[Ljava/lang/String;", null, l0, l1, 0);
                mv.visitMaxs(0, 1);
                mv.visitEnd();
            }
            cw.visitEnd();

            return cw.toByteArray();
        }
    }
}