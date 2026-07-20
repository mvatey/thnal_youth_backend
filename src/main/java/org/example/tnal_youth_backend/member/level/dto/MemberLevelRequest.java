package org.example.tnal_youth_backend.member.level.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberLevelRequest(

        @NotBlank(message = "Member level code is required")
        @Size(
                max = 30,
                message = "Member level code must not exceed 30 characters"
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