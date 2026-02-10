package com.securitymak.securitymak.exception;

public class CaseNotFoundException extends RuntimeException {

    public CaseNotFoundException() {
        super("Case not found");
    }

    public CaseNotFoundException(String message) {
        super(message);
    }
}