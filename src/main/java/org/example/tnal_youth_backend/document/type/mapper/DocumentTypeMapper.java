package org.example.tnal_youth_backend.document.type.mapper;

import org.example.tnal_youth_backend.document.type.dto.response.DocumentTypeResponse;
import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DocumentTypeMapper {

    public DocumentTypeResponse toResponse(
            DocumentType documentType
    ) {
        if (documentType == null) {
            return null;
        }

        return new DocumentTypeResponse(
                documentType.getId(),          // 1
                documentType.getCode(),        // 2
                documentType.getScope(),       // 3
                documentType.getLabelKm(),     // 4
                documentType.getLabelEn(),     // 5
                documentType.getDescription(), // 6
                documentType.getIsActive(),    // 7
                documentType.getSortOrder()    // 8
        );
    }

    public List<DocumentTypeResponse> toResponseList(
            List<DocumentType> documentTypes
    ) {
        if (documentTypes == null || documentTypes.isEmpty()) {
            return Collections.emptyList();
        }

        return documentTypes
                .stream()
                .map(this::toResponse)
                .toList();
    }
}