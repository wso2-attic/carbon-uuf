package org.wso2.carbon.uuf.core.create;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface ComponentReference {

    String FRAGMENTS_DIR_NAME = "fragments";

    Stream<PageReference> getPages(Set<String> supportedExtensions);

    Stream<FragmentReference> getFragments(Set<String> supportedExtensions);

    Optional<FileReference> getBindingsConfig();

    Optional<FileReference> getConfigurations();

    Optional<FileReference> getOsgiImportsConfig();

    FileReference resolveLayout(String layoutName);
}
