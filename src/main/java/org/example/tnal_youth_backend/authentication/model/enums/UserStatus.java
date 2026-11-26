package org.example.tnal_youth_backend.authentication.model.enums;

public enum UserStatus {
    PENDING_ACTIVATION,
    ACTIVE,
    INACTIVE,
    LOCKED,
    // Deleted by an admin or from the member page; hidden everywhere.
    DELETED
}