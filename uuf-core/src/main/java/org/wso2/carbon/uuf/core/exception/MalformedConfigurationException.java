package org.wso2.carbon.uuf.core.exception;

public class MalformedConfigurationException extends RuntimeException {
    public MalformedConfigurationException() {
    }

    public MalformedConfigurationException(String message) {
        super(message);
    }

    public MalformedConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedConfigurationException(Throwable cause) {
        super(cause);
    }

    public MalformedConfigurationException(String message, Throwable cause, boolean enableSuppression,
                                           boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
