package org.example.tnal_youth_backend.document.type.repository;

import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentTypeRepository
        extends JpaRepository<DocumentType, Short> {

    boolean
    existsByCodeIgnoreCase(
            String code
    );

    // Used by ActivityMediaServiceImpl#mirrorAttachmentAsDocument to resolve
    // the "ACTIVITY_DOCUMENT" type id -- documents.document_type_id is
    // NOT NULL, so a mirrored attachment must always resolve a real type id
    // before insert.
    Optional<DocumentType>
    findByCodeIgnoreCase(
            String code
    );

    List<DocumentType>
    findAllByOrderBySortOrderAscIdAsc();

    List<DocumentType>
    findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}