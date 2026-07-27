package org.example.tnal_youth_backend.document.type.repository;

import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentTypeRepository
        extends JpaRepository<DocumentType, Short> {

    List<DocumentType>
    findAllByIsActiveTrueOrderBySortOrderAscIdAsc();
}