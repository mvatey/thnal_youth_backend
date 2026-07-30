package org.example.tnal_youth_backend.document.type.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.document.type.dto.response.DocumentTypeResponse;
import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.example.tnal_youth_backend.document.type.enums.DocumentScope;
import org.example.tnal_youth_backend.document.type.enums.DocumentTypeCode;
import org.example.tnal_youth_backend.document.type.mapper.DocumentTypeMapper;
import org.example.tnal_youth_backend.document.type.repository.DocumentTypeRepository;
import org.example.tnal_youth_backend.document.type.service.DocumentTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentTypeServiceImpl
        implements DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentTypeMapper documentTypeMapper;

    @Override
    public List<DocumentTypeResponse> getActiveDocumentTypes() {
        List<DocumentType> documentTypes =
                documentTypeRepository
                        .findAllByIsActiveTrueOrderBySortOrderAscIdAsc();

        return documentTypeMapper.toResponseList(documentTypes);
    }

    @Override
    public List<DocumentTypeResponse> getActiveDocumentTypesByScope(
            DocumentScope scope
    ) {
        if (scope == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document scope is required"
            );
        }

        Set<String> typeCodes = switch (scope) {
            case INSTITUTIONAL ->
                    DocumentTypeCode.institutionalCodeNames();

            case MEMBER ->
                    DocumentTypeCode.memberCodeNames();
        };

        List<DocumentType> documentTypes =
                documentTypeRepository.findActiveByCodes(typeCodes);

        return documentTypeMapper.toResponseList(documentTypes);
    }

    @Override
    public List<DocumentTypeResponse> getActiveInstitutionalTypes() {
        return getActiveDocumentTypesByScope(
                DocumentScope.INSTITUTIONAL
        );
    }

    @Override
    public List<DocumentTypeResponse> getActiveMemberTypes() {
        return getActiveDocumentTypesByScope(
                DocumentScope.MEMBER
        );
    }

    @Override
    public DocumentTypeResponse getActiveDocumentTypeById(
            Short typeId
    ) {
        DocumentType documentType =
                requireActiveDocumentType(typeId);

        return documentTypeMapper.toResponse(documentType);
    }

    @Override
    public DocumentType requireActiveDocumentType(
            Short typeId
    ) {
        validateTypeId(typeId);

        return documentTypeRepository
                .findActiveById(typeId)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Active document type was not found with ID: "
                                        + typeId
                        )
                );
    }

    @Override
    public DocumentType requireActiveDocumentType(
            Short typeId,
            DocumentScope expectedScope
    ) {
        if (expectedScope == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expected document scope is required"
            );
        }

        DocumentType documentType =
                requireActiveDocumentType(typeId);

        DocumentScope actualScope =
                documentType.getScope();

        if (actualScope == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported document type code: "
                            + documentType.getCode()
            );
        }

        if (actualScope != expectedScope) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document type "
                            + documentType.getCode()
                            + " does not belong to scope "
                            + expectedScope.name()
            );
        }

        return documentType;
    }

    @Override
    public DocumentType requireActiveDocumentType(
            DocumentTypeCode expectedTypeCode
    ) {
        if (expectedTypeCode == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document type code is required"
            );
        }

        DocumentType documentType =
                documentTypeRepository
                        .findByCodeIgnoreCase(
                                expectedTypeCode.name()
                        )
                        .orElseThrow(
                                () -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Active document type was not found: "
                                                + expectedTypeCode.name()
                                )
                        );

        if (!documentType.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document type is inactive: "
                            + expectedTypeCode.name()
            );
        }

        return documentType;
    }

    @Override
    public boolean isInstitutionalType(
            Short typeId
    ) {
        DocumentType documentType =
                requireActiveDocumentType(typeId);

        return documentType.isInstitutionalType();
    }

    @Override
    public boolean isMemberType(
            Short typeId
    ) {
        DocumentType documentType =
                requireActiveDocumentType(typeId);

        return documentType.isMemberType();
    }

    private void validateTypeId(
            Short typeId
    ) {
        if (typeId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document type ID is required"
            );
        }

        if (typeId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document type ID must be positive"
            );
        }
    }
}