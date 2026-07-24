package org.example.tnal_youth_backend.authentication.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatus status =
                HttpStatus.valueOf(ex.getStatusCode().value());

        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .success(false)
                        .code(status.name())
                        .message(
                                ex.getReason() != null
                                        ? ex.getReason()
                                        : "Request failed"
                        )
                        .status(status.value())
                        .path(request.getRequestURI())
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error
                : ex.getBindingResult().getFieldErrors()) {

            errors.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .success(false)
                        .code("VALIDATION_ERROR")
                        .message("Invalid request data")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .path(request.getRequestURI())
                        .timestamp(OffsetDateTime.now())
                        .errors(errors)
                        .build()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(
                HttpStatus.UNAUTHORIZED
        ).body(
                ErrorResponse.builder()
                        .success(false)
                        .code("BAD_CREDENTIALS")
                        .message(
                                "Invalid phone/email or password"
                        )
                        .status(
                                HttpStatus.UNAUTHORIZED.value()
                        )
                        .path(request.getRequestURI())
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(
                HttpStatus.FORBIDDEN
        ).body(
                ErrorResponse.builder()
                        .success(false)
                        .code("ACCESS_DENIED")
                        .message(
                                "You do not have permission to access this resource"
                        )
                        .status(
                                HttpStatus.FORBIDDEN.value()
                        )
                        .path(request.getRequestURI())
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(
                HttpStatus.BAD_REQUEST
        ).body(
                ErrorResponse.builder()
                        .success(false)
                        .code("BAD_REQUEST")
                        .message(
                                ex.getMessage() != null
                                        ? ex.getMessage()
                                        : "Invalid request"
                        )
                        .status(
                                HttpStatus.BAD_REQUEST.value()
                        )
                        .path(request.getRequestURI())
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(
                HttpStatus.CONFLICT
        ).body(
                ErrorResponse.builder()
                        .success(false)
                        .code("INVALID_STATE")
                        .message(
                                ex.getMessage() != null
                                        ? ex.getMessage()
                                        : "Invalid application state"
                        )
                        .status(
                                HttpStatus.CONFLICT.value()
                        )
                        .path(request.getRequestURI())
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request
    ) {
        ex.printStackTrace();

        String message =
                ex.getMessage() != null
                        && !ex.getMessage().isBlank()
                        ? ex.getMessage()
                        : "Something went wrong";

        return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
        ).body(
                ErrorResponse.builder()
                        .success(false)
                        .code("INTERNAL_SERVER_ERROR")
                        .message(message)
                        .status(
                                HttpStatus.INTERNAL_SERVER_ERROR.value()
                        )
                        .path(request.getRequestURI())
                        .timestamp(OffsetDateTime.now())
                        .build()
        );
    }
}