package org.example.tnal_youth_backend.authentication.exception;

/**
 * Thrown when phone/email + password combination is invalid,
 * or when the user cannot be found by that identifier.
 * We use the SAME message for both cases so we never reveal
 * which one failed — this prevents attackers from using the
 * login endpoint to enumerate valid phone numbers / emails.
 */
public class InvalidCredentialException extends RuntimeException {
    public InvalidCredentialException(String message) {
        super(message);
    }
}