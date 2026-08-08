package org.example.tnal_youth_backend.authentication.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.example.tnal_youth_backend.common.exception.BusinessException;
import org.example.tnal_youth_backend.common.exception.ResourceNotFoundException;
import org.example.tnal_youth_backend.dashboard.exception.DashboardAccessException;
import org.example.tnal_youth_backend.dashboard.exception.InvalidDashboardMonthException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component("authGlobalExceptionHandler")
@RestControllerAdvice(basePackages = "org.example.tnal_youth_backend.authentication")
public class GlobalExceptionHandler {

    /*
     * ==========================================================
     * RESPONSE STATUS EXCEPTION
     * ==========================================================
     */

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse>
    handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatus status =
                HttpStatus.valueOf(
                        exception
                                .getStatusCode()
                                .value()
                );

        return buildErrorResponse(
                status,
                status.name(),
                exception.getReason() == null
                        ? status.getReasonPhrase()
                        : exception.getReason(),
                request,
                null
        );
    }

    /*
     * ==========================================================
     * BUSINESS EXCEPTION
     * ==========================================================
     */

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse>
    handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        HttpStatus status =
                exception.getStatus() == null
                        ? HttpStatus.BAD_REQUEST
                        : exception.getStatus();

        return buildErrorResponse(
                status,
                status.name(),
                exception.getMessage(),
                request,
                null
        );
    }

    /*
     * ==========================================================
     * VALIDATION
     * ==========================================================
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse>
    handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors =
                new HashMap<>();

        for (
                FieldError error
                : exception
                .getBindingResult()
                .getFieldErrors()
        ) {
            errors.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Invalid request data",
                request,
                errors
        );
    }

    /*
     * ==========================================================
     * AUTHENTICATION
     * ==========================================================
     */

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse>
    handleBadCredentials(
            BadCredentialsException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "BAD_CREDENTIALS",
                "Invalid phone/email or password",
                request,
                null
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse>
    handleAuthenticationException(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHENTICATED",
                exception.getMessage() == null
                        ? "Authentication is required"
                        : exception.getMessage(),
                request,
                null
        );
    }

    /*
     * ==========================================================
     * AUTHORIZATION
     * ==========================================================
     */

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse>
    handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to access this resource",
                request,
                null
        );
    }

    /*
     * ==========================================================
     * RESOURCE NOT FOUND
     * ==========================================================
     */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                exception.getMessage(),
                request,
                null
        );
    }

    /*
     * ==========================================================
     * DATABASE CONSTRAINTS
     * ==========================================================
     */

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse>
    handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "DATA_INTEGRITY_VIOLATION",
                "The operation violates a database constraint",
                request,
                null
        );
    }

    /*
     * ==========================================================
     * DASHBOARD
     * ==========================================================
     */

    @ExceptionHandler(InvalidDashboardMonthException.class)
    public ResponseEntity<ErrorResponse>
    handleInvalidDashboardMonth(
            InvalidDashboardMonthException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_DASHBOARD_MONTH",
                exception.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(DashboardAccessException.class)
    public ResponseEntity<ErrorResponse>
    handleDashboardAccessException(
            DashboardAccessException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "DASHBOARD_ACCESS_DENIED",
                exception.getMessage(),
                request,
                null
        );
    }

    /*
     * ==========================================================
     * UNKNOWN ERRORS
     * ==========================================================
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleGeneralException(
            Exception exception,
            HttpServletRequest request
    ) {
        exception.printStackTrace();

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Something went wrong",
                request,
                null
        );
    }

    /*
     * ==========================================================
     * RESPONSE BUILDER
     * ==========================================================
     */

    private ResponseEntity<ErrorResponse>
    buildErrorResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            Map<String, String> errors
    ) {
        ErrorResponse response =
                ErrorResponse.builder()
                        .success(false)
                        .code(code)
                        .message(message)
                        .status(status.value())
                        .path(
                                request.getRequestURI()
                        )
                        .timestamp(
                                OffsetDateTime.now()
                        )
                        .errors(errors)
                        .build();

        return ResponseEntity
                .status(status)
                .body(response);
    }
}