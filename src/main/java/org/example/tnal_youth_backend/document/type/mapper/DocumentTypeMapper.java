package org.example.tnal_youth_backend.document.type.mapper;

import org.example.tnal_youth_backend.document.type.dto.response.DocumentTypeResponse;
import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.springframework.stereotype.Component;

@Component
public class DocumentTypeMapper {

    public DocumentTypeResponse toResponse(
            DocumentType documentType
    ) {
        if (documentType == null) {
            return null;
        }

        return new DocumentTypeResponse(
                documentType.getId(),
                documentType.getCode(),
                documentType.getLabelKm(),
                documentType.getLabelEn(),
                documentType.getDescription(),
                documentType.getSortOrder()
        );
    }
}