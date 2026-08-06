package org.example.tnal_youth_backend.member.member.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MaritalStatus {

    SINGLE("SINGLE"),
    MARRIED("MARRIED");

    private final String value;

    MaritalStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MaritalStatus fromValue(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (MaritalStatus status : values()) {
            if (
                    status.value.equalsIgnoreCase(
                            value.trim()
                    )
            ) {
                return status;
            }
        }

        throw new IllegalArgumentException(
                "Invalid marital status: " + value
        );
    }
}