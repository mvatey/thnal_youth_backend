package org.example.tnal_youth_backend.authentication.exception;

/**
 * Thrown when a refresh token is missing, expired, revoked,
 * or does not belong to the requesting user.
 * Used by RefreshTokenService (Phase 5).
 */
public class RefreshTokenException extends RuntimeException {
    public RefreshTokenException(String message) {
        super(message);
    }
}