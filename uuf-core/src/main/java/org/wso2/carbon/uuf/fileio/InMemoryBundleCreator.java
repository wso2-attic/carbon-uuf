package org.wso2.carbon.uuf.fileio;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.BundleCreator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class InMemoryBundleCreator implements BundleCreator {

    private static final Logger log = LoggerFactory.getLogger(InMemoryBundleCreator.class);
    private static final String DUMMY_CLAZZ_PATH = "/bundle/create/DummyComponentBundle.claz";

    public Bundle createBundle(String name, String symbolicName, String version, Optional<List> exports,
                               Optional<List> imports) throws IOException, BundleException {
        InputStream bundleInputStream = createBundleStream(name, symbolicName, version, exports, imports);
        Bundle tBundle = FrameworkUtil.getBundle(InMemoryBundleCreator.class);
        BundleContext bundleContext = tBundle.getBundleContext();
        return bundleContext.installBundle("", bundleInputStream);
    }

    private InputStream createBundleStream(String name, String symbolicName, String version,
                                           Optional<List> exports, Optional<List> imports)
            throws IOException {
        Manifest bundleManifest = new Manifest();
        Attributes attributes = bundleManifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.putValue("Bundle-ManifestVersion", "2");
        attributes.putValue("Bundle-Name", name);
        attributes.putValue("Bundle-SymbolicName", symbolicName);
        attributes.putValue("Bundle-Version", version);

        ByteArrayOutputStream jarOutputStream = new ByteArrayOutputStream();
        try (JarOutputStream target = new JarOutputStream(jarOutputStream, bundleManifest)) {
            InputStream resource = InMemoryBundleCreator.class.getResourceAsStream(DUMMY_CLAZZ_PATH);
            byte[] data = IOUtils.toByteArray(resource);
            add("DummyComponentBundle.class", data, target);
        }
        return new ByteArrayInputStream(jarOutputStream.toByteArray());
    }

    private void add(String fileName, byte[] content, JarOutputStream target) throws IOException {
        JarEntry entry = new JarEntry(fileName.replace("\\", "/"));
        target.putNextEntry(entry);
        target.write(content);
        target.closeEntry();
    }

}