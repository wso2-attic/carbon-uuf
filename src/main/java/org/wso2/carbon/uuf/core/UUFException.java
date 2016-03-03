package org.wso2.carbon.uuf.core;

import javax.ws.rs.core.Response;

public class UUFException extends RuntimeException {
    private Response.Status status;

    public UUFException(String message, Response.Status status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public UUFException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }
}
