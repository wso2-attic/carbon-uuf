package org.wso2.carbon.uuf.fileio;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uuf.core.BundleCreator;
import org.wso2.carbon.uuf.core.UUFException;
import org.wso2.carbon.uuf.core.create.ComponentReference;

import java.io.*;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class InMemoryBundleCreator implements BundleCreator {

    private static final Logger log = LoggerFactory.getLogger(InMemoryBundleCreator.class);
    private static final String DUMMY_CLASS_PATH = "/bundle/create/DummyComponentBundle.claz";
    private static final String DUMMY_CLASS_NAME = "DummyComponentBundle.class";

    /**
     * Creates a new OSGi bundle. Created bundle is reusable across multiple UUF Apps.
     * @param compReference component reference
     * @return created OSGi bundle
     * @see #getBundleClassLoader(String)
     */
    public Bundle createBundle(ComponentReference compReference) {
        String name = getBundleName(compReference.getApp().getName(), compReference.getName());
        String symbolicName = compReference.getName();
        String version = compReference.getVersion();
        String locationKey = getBundleLocationKey(compReference.getName(), compReference.getContext());
        try {
            Optional<List<String>> imports = Optional.of(getImports(compReference));
            InputStream bundleInputStream = createBundleStream(name, symbolicName, version, imports);
            Bundle currentBundle = FrameworkUtil.getBundle(InMemoryBundleCreator.class);
            BundleContext bundleContext = currentBundle.getBundleContext();
            Bundle bundle = bundleContext.installBundle(locationKey, bundleInputStream);
            bundle.start();
            return bundle;
        } catch (BundleException e) {
            throw new UUFException("Error while installing the bundle of " + symbolicName, e);
        } catch (IOException e) {
            throw new UUFException("Error while creating the bundle of " + symbolicName, e);
        }
    }

    private List<String> getImports(ComponentReference componentReference) throws IOException{
        List<String> imports = new ArrayList<>();
        if (!componentReference.getOsgiImportsConfig().isPresent()) {
            return imports;
        }
        String content = componentReference.getOsgiImportsConfig().get().getContent();
        Properties osgiImportConfig = new Properties();
        osgiImportConfig.load(new StringReader(content));
        String importList = ((String)osgiImportConfig.get("import.package")).replaceAll("\\r|\\n", "");
        Collections.addAll(imports, importList.split(","));
        return imports;
    }

    /**
     * Returns OSGi Bundle Class Loader for a OSGi bundle given by location key.
     * Use {@link #getBundleLocationKey(String, String)} to retrieve locationKey.
     * @param locationKey location key
     * @return OSGi bundle class loader
     * @see #getBundleLocationKey(String, String)
     */
    public ClassLoader getBundleClassLoader(String locationKey) {
        Bundle currentBundle = FrameworkUtil.getBundle(InMemoryBundleCreator.class);
        BundleContext bundleContext = currentBundle.getBundleContext();
        Bundle bundle = bundleContext.getBundle(locationKey);
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        return bundleWiring.getClassLoader();
    }

    private InputStream createBundleStream(String name, String symbolicName, String version, Optional<List<String>> imports)
            throws IOException {
        Manifest bundleManifest = new Manifest();
        Attributes attributes = bundleManifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.putValue("Bundle-ManifestVersion", "2");
        attributes.putValue("Bundle-Name", name);
        attributes.putValue("Bundle-SymbolicName", symbolicName);
        attributes.putValue("Bundle-Version", version);

        if(imports.isPresent()) {
            attributes.putValue("Import-Package", String.join(",", imports.get()));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JarOutputStream target = new JarOutputStream(outputStream, bundleManifest)) {
            InputStream resource = InMemoryBundleCreator.class.getResourceAsStream(DUMMY_CLASS_PATH);
            byte[] data = IOUtils.toByteArray(resource);
            addJarEntry(DUMMY_CLASS_NAME, data, target);
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private void addJarEntry(String fileName, byte[] content, JarOutputStream target) throws IOException {
        JarEntry entry = new JarEntry(fileName.replace("\\", "/"));
        target.putNextEntry(entry);
        target.write(content);
        target.closeEntry();
    }

    public String getBundleName(String appName, String name) {
        return "UUF bundle for " + getBundleLocationKey(appName, name);
    }

    public String getBundleLocationKey(String appName, String name) {
        return name.equals("root") ? appName : name;
    }

}