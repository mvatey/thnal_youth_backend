package org.example.tnal_youth_backend.document.type.service;

import org.example.tnal_youth_backend.document.type.dto.response.DocumentTypeResponse;
import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.example.tnal_youth_backend.document.type.enums.DocumentScope;
import org.example.tnal_youth_backend.document.type.enums.DocumentTypeCode;

import java.util.List;

public interface DocumentTypeService {

    /**
     * Returns every active document type.
     */
    List<DocumentTypeResponse> getActiveDocumentTypes();

    /**
     * Returns active document types belonging to a specific scope.
     *
     * Supported scopes:
     *
     * - INSTITUTIONAL
     * - MEMBER
     */
    List<DocumentTypeResponse> getActiveDocumentTypesByScope(
            DocumentScope scope
    );

    /**
     * Returns active institutional document types.
     */
    List<DocumentTypeResponse> getActiveInstitutionalTypes();

    /**
     * Returns active member document types.
     */
    List<DocumentTypeResponse> getActiveMemberTypes();

    /**
     * Returns one active document type by ID.
     */
    DocumentTypeResponse getActiveDocumentTypeById(
            Short typeId
    );

    /**
     * Internal service method used when creating or updating documents.
     *
     * Returns the entity instead of a response DTO.
     */
    DocumentType requireActiveDocumentType(
            Short typeId
    );

    /**
     * Requires a document type and verifies that it belongs to the expected
     * scope.
     */
    DocumentType requireActiveDocumentType(
            Short typeId,
            DocumentScope expectedScope
    );

    /**
     * Requires a document type with a specific code.
     */
    DocumentType requireActiveDocumentType(
            DocumentTypeCode expectedTypeCode
    );

    /**
     * Checks whether a type belongs to the institutional scope.
     */
    boolean isInstitutionalType(
            Short typeId
    );

    /**
     * Checks whether a type belongs to the member scope.
     */
    boolean isMemberType(
            Short typeId
    );
}