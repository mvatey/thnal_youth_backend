package org.example.tnal_youth_backend.document.document.mapper;

import org.example.tnal_youth_backend.document.document.dto.response.DocumentResponse;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponse toResponse(
            Document document
    ) {
        if (document == null) {
            return null;
        }

        return new DocumentResponse(
                document.getId(),
                document.getTypeId(),
                document.getFileId(),
                document.getTitle(),
                document.getDescription(),
                document.getBranchId(),
                document.getMemberId(),
                document.getActivityId(),
                document.getUploadedById(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}