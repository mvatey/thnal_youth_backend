package org.example.tnal_youth_backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    /*
     * Used by the restored donation and SecurityUtils code:
     *
     * new BusinessException(
     *     "ERROR_CODE",
     *     "Error message"
     * );
     */
    public BusinessException(
            String code,
            String message
    ) {
        super(message);
        this.code = code;
        this.status = defaultStatusFor(code);
    }

    /*
     * Use when a specific HTTP status is required.
     */
    public BusinessException(
            String code,
            String message,
            HttpStatus status
    ) {
        super(message);
        this.code = code;
        this.status = status == null
                ? HttpStatus.BAD_REQUEST
                : status;
    }

    public BusinessException(
            String code,
            String message,
            HttpStatus status,
            Throwable cause
    ) {
        super(message, cause);
        this.code = code;
        this.status = status == null
                ? HttpStatus.BAD_REQUEST
                : status;
    }

    private static HttpStatus defaultStatusFor(
            String code
    ) {
        if (code == null) {
            return HttpStatus.BAD_REQUEST;
        }

        return switch (code) {
            case "UNAUTHENTICATED" ->
                    HttpStatus.UNAUTHORIZED;

            case "FORBIDDEN",
                 "ACCESS_DENIED" ->
                    HttpStatus.FORBIDDEN;

            case "RESOURCE_NOT_FOUND",
                 "DONATION_NOT_FOUND",
                 "MEMBER_NOT_FOUND",
                 "BRANCH_NOT_FOUND",
                 "MONTHLY_DONATION_TYPE_NOT_FOUND" ->
                    HttpStatus.NOT_FOUND;

            case "ALREADY_EXISTS",
                 "MONTHLY_DONATION_ALREADY_EXISTS",
                 "DUPLICATE_DONATION" ->
                    HttpStatus.CONFLICT;

            default ->
                    HttpStatus.BAD_REQUEST;
        };
    }
}