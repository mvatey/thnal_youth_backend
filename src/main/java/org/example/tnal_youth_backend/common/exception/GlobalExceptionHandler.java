package org.example.tnal_youth_backend.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.common.response.ApiResponse;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

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
        HttpStatus status = exception.getStatus() == null
                ? HttpStatus.BAD_REQUEST
                : exception.getStatus();

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
     * RESOURCE NOT FOUND
     * ==========================================================
     */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleResourceNotFoundException(
            ResourceNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.error(
                                "RESOURCE_NOT_FOUND",
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
                        .or(() ->
                                /*
                                 * A class-level constraint (e.g. @AssertTrue
                                 * on a boolean method spanning multiple
                                 * fields, like "phone or email required")
                                 * surfaces as a global error, not a field
                                 * error -- check here too so its message
                                 * isn't silently dropped.
                                 */
                                exception
                                        .getBindingResult()
                                        .getGlobalErrors()
                                        .stream()
                                        .findFirst()
                                        .map(org.springframework.validation.ObjectError::getDefaultMessage)
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

    /*
     * A duplicate value SHOULD already be caught by a proactive
     * existsBy...() check in the service layer before it ever reaches the
     * database -- that's what produces the specific "X already exists"
     * ResponseStatusExceptions elsewhere in this file. This handler is the
     * fallback for whenever a proactive check is missing or has a gap
     * (this codebase has found more than one such gap already): rather
     * than always showing the same generic message, name the actual
     * duplicated field when the violated constraint is one we recognize,
     * so a real bug in a proactive check doesn't also hide which field
     * broke from the person trying to fix it.
     */
    private static final Map<String, String>
            KNOWN_UNIQUE_CONSTRAINT_MESSAGES =
            Map.ofEntries(
                    Map.entry(
                            "uq_members_phone",
                            "This phone number already exists. Please use a different one."
                    ),
                    Map.entry(
                            "uq_members_email",
                            "This email already exists. Please use a different one."
                    ),
                    Map.entry(
                            "users_phone_key",
                            "This phone number is already used by another account"
                    ),
                    Map.entry(
                            "uq_users_email_lower",
                            "This email is already used by another account"
                    ),
                    Map.entry(
                            "uq_users_username",
                            "This username is already used by another account"
                    )
            );

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

        String constraintName =
                extractConstraintName(exception);

        String message =
                constraintName != null
                        ? KNOWN_UNIQUE_CONSTRAINT_MESSAGES
                                .getOrDefault(
                                        constraintName,
                                        "The request conflicts with existing data"
                                )
                        : "The request conflicts with existing data";

        return ResponseEntity
                .status(
                        HttpStatus.CONFLICT
                )
                .body(
                        ApiResponse.error(
                                "DATA_INTEGRITY_VIOLATION",
                                message
                        )
                );
    }

    private String extractConstraintName(
            Throwable exception
    ) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolation) {
                return constraintViolation.getConstraintName();
            }

            current = current.getCause();
        }

        return null;
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
     * UPLOAD TOO LARGE
     * ==========================================================
     *
     * Thrown by the servlet multipart layer itself -- BEFORE the request
     * ever reaches a controller method -- whenever a file exceeds
     * spring.servlet.multipart.max-file-size/max-request-size (see
     * application.properties). Without this handler it fell through to
     * the generic 500 below, which is exactly the "Something went wrong"
     * a user hitting an oversized upload would see instead of a message
     * telling them the file was too big.
     */

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(
                        ApiResponse.error(
                                "PAYLOAD_TOO_LARGE",
                                "The uploaded file is too large"
                        )
                );
    }


    /*
     * ==========================================================
     * UNSUPPORTED HTTP METHOD / UNREADABLE REQUEST BODY
     * ==========================================================
     *
     * Framework-level request problems that also used to fall through to
     * the generic 500 handler below instead of a proper 4xx.
     */

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(
                        ApiResponse.error(
                                "METHOD_NOT_ALLOWED",
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleMessageNotReadable(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.error(
                                "MALFORMED_REQUEST_BODY",
                                "The request body could not be read"
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
