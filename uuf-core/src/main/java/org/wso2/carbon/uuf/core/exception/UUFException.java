package org.wso2.carbon.uuf.core.exception;

public class UUFException extends RuntimeException {

    public UUFException() {
    }

    public UUFException(Throwable cause) {
        super(cause);
    }

    public UUFException(String message, Throwable cause) {
        super(message, cause);
    }

    public UUFException(String message) {
        super(message);
    }
}
