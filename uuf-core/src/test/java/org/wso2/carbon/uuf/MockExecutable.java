package org.wso2.carbon.uuf;

import com.google.common.collect.ImmutableMap;
import org.wso2.carbon.uuf.core.Executable;

public class MockExecutable implements Executable {

    @Override
    public Object execute() {
        return ImmutableMap.of("name", "Fry");
    }
}
