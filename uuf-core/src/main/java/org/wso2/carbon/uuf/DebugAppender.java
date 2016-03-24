package org.wso2.carbon.uuf;

import com.google.gson.Gson;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DebugAppender extends AppenderSkeleton {

    private static final int CAPACITY = 1000;
    private final ConcurrentLinkedQueue<DebugMessage> messages = new ConcurrentLinkedQueue<>();

    public void attach() {
        setThreshold(Level.DEBUG);
        Logger logger = Logger.getLogger("org.wso2.carbon.uuf");
        logger.setLevel(Level.DEBUG);
        Logger.getRootLogger().addAppender(this);
    }

    @Override
    public void append(LoggingEvent event) {
        String requestId = (String) event.getMDC("uuf-request");
        if (requestId != null) {
            messages.add(new DebugMessage(requestId, event));
            if (messages.size() > CAPACITY) {
                messages.poll();
            }
        }
    }

    public String asJson() {
        Gson gson = new Gson();
        return gson.toJson(messages);
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    public void close() {
    }

    private static class DebugMessage {
        private final String requestId;
        private final LoggingEvent event;

        DebugMessage(String requestId, LoggingEvent event) {
            this.requestId = requestId;
            this.event = event;
        }
    }
}
