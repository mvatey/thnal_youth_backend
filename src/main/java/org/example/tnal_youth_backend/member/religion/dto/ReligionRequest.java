package org.example.tnal_youth_backend.member.religion.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReligionRequest(

        @NotBlank(message = "Religion code is required")
        @Size(
                max = 30,
                message = "Religion code must not exceed 30 characters"
        )
        @Pattern(
                regexp = "^[A-Za-z][A-Za-z0-9_-]*$",
                message = "Religion code may contain only letters, numbers, underscores, and hyphens"
        )
        String code,

        @NotBlank(message = "Khmer label is required")
        @Size(
                max = 100,
                message = "Khmer label must not exceed 100 characters"
        )
        String labelKm,

        @Size(
                max = 100,
                message = "English label must not exceed 100 characters"
        )
        String labelEn,

        String description,

        Boolean isActive,

        @Min(
                value = 0,
                message = "Sort order cannot be negative"
        )
        Integer sortOrder
) {
}