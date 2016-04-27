package org.wso2.carbon.uuf.handlebars;

import org.wso2.carbon.uuf.core.API;

public interface Executable {
    Object execute(Object context, API api);
}
