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
import org.wso2.carbon.uuf.internal.core.create.ClassLoaderProvider;
import org.wso2.carbon.uuf.core.Component;
import org.wso2.carbon.uuf.reference.ComponentReference;
import org.wso2.carbon.uuf.core.exception.UUFException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class BundleClassLoaderProvider implements ClassLoaderProvider {

    private static final String DUMMY_CLASS_PATH = "/bundle/create/DummyComponentBundle.claz";
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
    public ClassLoader getClassLoader(String appName, String componentName, String componentVersion,
                                      ComponentReference componentReference) {
        Bundle bundle = createBundleIfNotExists(appName, componentName, componentVersion, componentReference);
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        return bundleWiring.getClassLoader();
    }

    /**
     * If no bundle exists for provided component reference, It create and returns a new OSGi bundle. Or else it will
     * return the existing bundle. Created bundle is reusable across multiple UUF Apps.
     *
     * @return created OSGi bundle
     */
    private Bundle createBundleIfNotExists(String appName, String componentName, String componentVersion,
                                           ComponentReference componentReference) {
        String bundleKey = getBundleKey(appName, componentName);
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        Bundle bundle = bundleContext.getBundle(bundleKey);
        if (bundle != null) {
            return bundle;
        }

        String bundleName = getBundleName(appName, componentName);
        componentVersion = processSnapshotVersion(componentVersion);
        try {
            InputStream bundleInputStream = createBundleStream(bundleName, bundleKey, componentVersion,
                                                               getImports(componentReference));
            bundle = bundleContext.installBundle(bundleKey, bundleInputStream);
            bundle.start();
            return bundle;
        } catch (IOException e) {
            throw new UUFException("Error while creating the OSGi bundle for component '" + componentName + "-" +
                                           componentVersion + "' in app '" + appName + "'.", e);
        } catch (BundleException e) {
            throw new UUFException("Error while installing the OSGi bundle of component '" + componentName + "-" +
                                           componentVersion + "' in app '" + appName + "'.", e);
        }
    }

    private String processSnapshotVersion(String version) {
        version = version.replace("-SNAPSHOT", ".SNAPSHOT");
        return version;
    }

    private Optional<List<String>> getImports(ComponentReference componentReference) throws IOException {
        if (!componentReference.getOsgiImportsConfig().isPresent()) {
            return Optional.empty();
        }
        String content = componentReference.getOsgiImportsConfig().get().getContent();
        Properties osgiImportConfig = new Properties();
        osgiImportConfig.load(new StringReader(content));
        String importList = osgiImportConfig.getProperty("import.package");
        if (importList == null) {
            return Optional.empty();
        } else {
            importList = importList.replaceAll("\\r|\\n", "");
            List<String> imports = new ArrayList<>();
            Collections.addAll(imports, importList.split(","));
            return Optional.of(imports);
        }
    }

    private InputStream createBundleStream(String name, String symbolicName, String version,
                                           Optional<List<String>> imports)
            throws IOException {
        Manifest bundleManifest = new Manifest();
        Attributes attributes = bundleManifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.putValue("Bundle-ManifestVersion", "2");
        attributes.putValue("Bundle-Name", name);
        attributes.putValue("Bundle-SymbolicName", symbolicName);
        attributes.putValue("Bundle-Version", version);

        if (imports.isPresent()) {
            attributes.putValue("Import-Package", String.join(",", imports.get()));
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

    private String getBundleKey(String appName, String componentName) {
        return Component.ROOT_COMPONENT_NAME.equals(componentName) ? (appName + "-" + componentName) : componentName;
    }

    private String getBundleName(String appName, String componentName) {
        return "UUF bundle for " + getBundleKey(appName, componentName);
    }

    /**
     * This class intends to use as dummy class for the OSGi bundle.
     * Auto-generated using ASM plugin.
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