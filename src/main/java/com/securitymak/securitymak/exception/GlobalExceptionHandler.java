package com.securitymak.securitymak.exception;

import com.securitymak.securitymak.exception.CaseNotFoundException;
import com.securitymak.securitymak.exception.InvalidCaseTransitionException;
import com.securitymak.securitymak.exception.UnauthorizedCaseAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 — Case not found
    @ExceptionHandler(CaseNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleCaseNotFound(CaseNotFoundException ex) {
        return ApiError.of(
                HttpStatus.NOT_FOUND.value(),
                "CASE_NOT_FOUND",
                ex.getMessage() != null ? ex.getMessage() : "Case not found"
        );
    }

    // 400 — Invalid state transition
    @ExceptionHandler(InvalidCaseTransitionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidTransition(InvalidCaseTransitionException ex) {
        return ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_CASE_STATUS_TRANSITION",
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Invalid case status transition"
        );
    }

    // 403 — Cross-tenant / role violations
    @ExceptionHandler({
            UnauthorizedCaseAccessException.class,
            AccessDeniedException.class
    })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDenied(RuntimeException ex) {
        return ApiError.of(
                HttpStatus.FORBIDDEN.value(),
                "ACCESS_DENIED",
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "You are not allowed to perform this action"
        );
    }

    // 400 — Validation / Illegal arguments
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException ex) {
        return ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                ex.getMessage()
        );
    }

    // 500 — Fallback (do NOT leak details)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneric(Exception ex) {
        return ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred"
        );
    }

    // 401 — Authentication failures
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleUnauthorized(UnauthorizedException ex) {
        return ApiError.of(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                ex.getMessage() != null ? ex.getMessage() : "Invalid credentials"
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(ResourceNotFoundException ex) {
        return ApiError.of(404, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBusinessRule(BusinessRuleViolationException ex) {
        return ApiError.of(400, "BUSINESS_RULE_VIOLATION", ex.getMessage());
    }
    
}