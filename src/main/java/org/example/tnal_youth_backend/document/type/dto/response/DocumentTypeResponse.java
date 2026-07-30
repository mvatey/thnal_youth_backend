package org.example.tnal_youth_backend.document.type.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.tnal_youth_backend.document.type.enums.DocumentScope;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentTypeResponse(
        Short id,
        String code,
        DocumentScope scope,
        String labelKm,
        String labelEn,
        String description,
        Boolean isActive,
        Integer sortOrder
) {
}