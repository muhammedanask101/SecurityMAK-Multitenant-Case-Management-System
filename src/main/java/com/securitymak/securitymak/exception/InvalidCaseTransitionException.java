package com.securitymak.securitymak.exception;

public class InvalidCaseTransitionException extends RuntimeException {

    public InvalidCaseTransitionException() {
        super("Invalid case status transition");
    }

    public InvalidCaseTransitionException(String message) {
        super(message);
    }
}