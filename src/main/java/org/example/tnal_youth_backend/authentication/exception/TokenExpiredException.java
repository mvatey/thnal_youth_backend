package org.example.tnal_youth_backend.authentication.exception;

/**
 * Thrown when a JWT access token has expired.
 * Used by JwtAuthenticationFilter (Phase 4).
 */
public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }
}