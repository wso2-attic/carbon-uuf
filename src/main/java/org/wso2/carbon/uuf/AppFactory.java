package org.wso2.carbon.uuf;

import org.wso2.carbon.uuf.core.App;

public interface AppFactory {

    App createApp(String name, String context);
}
