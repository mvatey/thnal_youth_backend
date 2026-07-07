package org.example.tnal_youth_backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private static final Clock UTC_CLOCK = Clock.system(ZoneOffset.UTC);

    private final boolean success;
    private final T data;
    private final String errorCode;
    private final String message;
    private final OffsetDateTime timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null, OffsetDateTime.now(UTC_CLOCK));
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, null, message, OffsetDateTime.now(UTC_CLOCK));
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, code, message, OffsetDateTime.now(UTC_CLOCK));
    }
}