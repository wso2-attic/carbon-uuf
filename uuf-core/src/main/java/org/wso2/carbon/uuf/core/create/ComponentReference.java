package org.wso2.carbon.uuf.core.create;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface ComponentReference {

    String DIR_NAME_PAGES = "pages";
    String DIR_NAME_LAYOUTS = "layouts";
    String DIR_NAME_FRAGMENTS = "fragments";
    String FILE_NAME_BINDINGS = "bindings.yaml";
    String FILE_NAME_CONFIGURATIONS = "config.yaml";
    String FILE_NAME_OSGI_IMPORTS = "osgi-imports.properties";

    Stream<PageReference> getPages(Set<String> supportedExtensions);

    Stream<FragmentReference> getFragments(Set<String> supportedExtensions);

    Optional<FileReference> getBindingsConfig();

    Optional<FileReference> getConfigurations();

    Optional<FileReference> getOsgiImportsConfig();

    FileReference resolveLayout(String layoutName);
}
