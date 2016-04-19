package org.wso2.carbon.uuf.core.create;

import org.wso2.carbon.uuf.fileio.ArtifactAppReference;

public interface AppResolver {

    AppReference resolve(String appName);
}