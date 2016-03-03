package org.wso2.carbon.uuf;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class DebugAppender extends AppenderSkeleton {

    @Override
    protected void append(LoggingEvent event) {
        String requestId = (String) event.getMDC("uuf-request");
        if (requestId != null) {
            System.out.print("#" + requestId + " ");
        }
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    public void close() {

    }

    public static void attach() {
        DebugAppender appender = new DebugAppender();
        appender.setThreshold(Level.DEBUG);
        Logger logger = Logger.getLogger("org.wso2.carbon.uuf");
        logger.setLevel(Level.DEBUG);
        Logger.getRootLogger().addAppender(appender);
    }
}
