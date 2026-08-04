package org.example.tnal_youth_backend.member.member.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum TshirtSize {

    XS("XS"),
    S("S"),
    M("M"),
    L("L"),
    XL("XL"),
    TWO_XL("2XL"),
    THREE_XL("3XL");

    private final String value;

    TshirtSize(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TshirtSize fromValue(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(size ->
                        size.value.equalsIgnoreCase(
                                value.trim()
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid T-shirt size: "
                                        + value
                        )
                );
    }
}