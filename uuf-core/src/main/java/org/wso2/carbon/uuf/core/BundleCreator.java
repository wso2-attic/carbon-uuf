package org.wso2.carbon.uuf.core;

import org.osgi.framework.Bundle;
import org.wso2.carbon.uuf.core.create.ComponentReference;

import java.util.List;
import java.util.Optional;

public interface BundleCreator {

    Bundle createBundleIfNotExists(ComponentReference componentReference);

    ClassLoader getComponentBundleClassLoader(Bundle bundle);
}