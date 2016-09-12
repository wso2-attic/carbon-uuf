package org.wso2.carbon.uuf.spi;

public interface DebugLogTailerListner {

    void hasNewLogLine(String line);
}