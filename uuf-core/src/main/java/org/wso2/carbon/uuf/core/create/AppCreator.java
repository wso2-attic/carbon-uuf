package org.wso2.carbon.uuf.core.create;

import org.wso2.carbon.uuf.core.App;

public interface AppCreator {
    App createApp(String appName, String context);
}
