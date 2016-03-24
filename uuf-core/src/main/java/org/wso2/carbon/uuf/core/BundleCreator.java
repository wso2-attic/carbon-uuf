package org.wso2.carbon.uuf.core;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface BundleCreator {

    Bundle createBundle(String name, String symbolicName, String version, Optional<List> exports,
                        Optional<List> imports) throws IOException,BundleException;

}