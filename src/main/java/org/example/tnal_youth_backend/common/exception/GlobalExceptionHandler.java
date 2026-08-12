package org.example.tnal_youth_backend.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.common.response.ApiResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * ==========================================================
     * BUSINESS EXCEPTION
     * ==========================================================
     */

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleBusinessException(
            BusinessException exception
    ) {
        HttpStatus status;

        if (
                "UNAUTHENTICATED"
                        .equals(
                                exception.getCode()
                        )
        ) {
            status =
                    HttpStatus.UNAUTHORIZED;

        } else if (
                "FORBIDDEN"
                        .equals(
                                exception.getCode()
                        )
        ) {
            status =
                    HttpStatus.FORBIDDEN;

        } else if (
                "NOT_FOUND"
                        .equals(
                                exception.getCode()
                        )
        ) {
            status =
                    HttpStatus.NOT_FOUND;

        } else if (
                "CONFLICT"
                        .equals(
                                exception.getCode()
                        )
        ) {
            status =
                    HttpStatus.CONFLICT;

        } else {
            status =
                    HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity
                .status(status)
                .body(
                        ApiResponse.error(
                                exception.getCode(),
                                exception.getMessage()
                        )
                );
    }


    /*
     * ==========================================================
     * REQUEST VALIDATION
     * ==========================================================
     */

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<ApiResponse<Void>>
    handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        String message =
                exception
                        .getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .findFirst()
                        .map(error ->
                                error.getField()
                                        + ": "
                                        + error.getDefaultMessage()
                        )
                        .orElse(
                                "Request validation failed"
                        );

        return ResponseEntity
                .status(
                        HttpStatus.BAD_REQUEST
                )
                .body(
                        ApiResponse.error(
                                "VALIDATION_FAILED",
                                message
                        )
                );
    }


    /*
     * ==========================================================
     * RESPONSE STATUS EXCEPTION
     * ==========================================================
     *
     * Keeps the status created by the service layer.
     *
     * Example:
     *
     * throw new ResponseStatusException(
     *     HttpStatus.CONFLICT,
     *     "Phone number already exists"
     * );
     *
     * Result:
     *
     * HTTP 409
     * {
     *   "success": false,
     *   "code": "CONFLICT",
     *   "message": "Phone number already exists"
     * }
     */

    @ExceptionHandler(
            ResponseStatusException.class
    )
    public ResponseEntity<ApiResponse<Void>>
    handleResponseStatusException(
            ResponseStatusException exception
    ) {
        HttpStatus status =
                HttpStatus.valueOf(
                        exception
                                .getStatusCode()
                                .value()
                );

        String message =
                exception.getReason() != null
                        ? exception.getReason()
                        : status
                        .getReasonPhrase();

        /*
         * Expected client/business errors should not
         * be logged as server failures.
         */
        if (
                status.is4xxClientError()
        ) {
            log.warn(
                    "{} {}",
                    status.value(),
                    message
            );
        } else {
            log.error(
                    "{} {}",
                    status.value(),
                    message,
                    exception
            );
        }

        return ResponseEntity
                .status(status)
                .body(
                        ApiResponse.error(
                                status.name(),
                                message
                        )
                );
    }


    /*
     * ==========================================================
     * DATABASE CONSTRAINT ERROR
     * ==========================================================
     */

    @ExceptionHandler(
            DataIntegrityViolationException.class
    )
    public ResponseEntity<ApiResponse<Void>>
    handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        log.warn(
                "Database constraint violation",
                exception
        );

        return ResponseEntity
                .status(
                        HttpStatus.CONFLICT
                )
                .body(
                        ApiResponse.error(
                                "DATA_INTEGRITY_VIOLATION",
                                "The request conflicts with existing data"
                        )
                );
    }


    /*
     * ==========================================================
     * AUTHENTICATION
     * ==========================================================
     */

    @ExceptionHandler(
            AuthenticationException.class
    )
    public ResponseEntity<ApiResponse<Void>>
    handleAuthenticationException(
            AuthenticationException exception
    ) {
        return ResponseEntity
                .status(
                        HttpStatus.UNAUTHORIZED
                )
                .body(
                        ApiResponse.error(
                                "UNAUTHENTICATED",
                                "Authentication is required"
                        )
                );
    }


    /*
     * ==========================================================
     * AUTHORIZATION
     * ==========================================================
     */

    @ExceptionHandler(
            AccessDeniedException.class
    )
    public ResponseEntity<ApiResponse<Void>>
    handleAccessDeniedException(
            AccessDeniedException exception
    ) {
        return ResponseEntity
                .status(
                        HttpStatus.FORBIDDEN
                )
                .body(
                        ApiResponse.error(
                                "FORBIDDEN",
                                "You do not have permission to perform this action"
                        )
                );
    }


    /*
     * ==========================================================
     * UNEXPECTED SERVER ERROR
     * ==========================================================
     *
     * Only truly unexpected exceptions should reach here.
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>>
    handleUnexpectedException(
            Exception exception
    ) {
        log.error(
                "Unhandled request failure",
                exception
        );

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(
                        ApiResponse.error(
                                "INTERNAL_SERVER_ERROR",
                                "Something went wrong"
                        )
                );
    }
}