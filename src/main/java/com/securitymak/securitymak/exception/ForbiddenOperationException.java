package com.securitymak.securitymak.exception;

public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }

    public ForbiddenOperationException() {
        super("Forbidden operation");
    }
}
