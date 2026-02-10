package com.securitymak.securitymak.exception;

public class UnauthorizedCaseAccessException extends RuntimeException {

    public UnauthorizedCaseAccessException() {
        super("Unauthorized case access");
    }

    public UnauthorizedCaseAccessException(String message) {
        super(message);
    }
}