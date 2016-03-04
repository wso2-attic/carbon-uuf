package org.wso2.carbon.uuf.core;

import javax.ws.rs.core.Response;

public class UUFException extends RuntimeException {
    private final Response.Status status;

    public UUFException(String message, Throwable cause) {
        super(message, cause);
        this.status = Response.Status.INTERNAL_SERVER_ERROR;
    }

    public UUFException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }
}
