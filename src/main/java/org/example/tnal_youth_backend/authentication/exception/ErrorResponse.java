package org.example.tnal_youth_backend.authentication.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {

    private boolean success;

    private String code;

    private String message;

    private int status;

    private String path;

    private OffsetDateTime timestamp;

    private Map<String, String> errors;
}