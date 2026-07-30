package org.example.tnal_youth_backend.document.document.service.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.activity.entity.Activity;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.document.document.dto.request.DocumentFilterRequest;
import org.example.tnal_youth_backend.document.document.dto.request.InstitutionalDocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.request.MemberDocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentDetailResponse;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentPageResponse;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.example.tnal_youth_backend.document.document.mapper.DocumentMapper;
import org.example.tnal_youth_backend.document.document.repository.DocumentRepository;
import org.example.tnal_youth_backend.document.document.service.DocumentService;
import org.example.tnal_youth_backend.document.document.specification.DocumentSpecification;
import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.example.tnal_youth_backend.document.type.enums.DocumentScope;
import org.example.tnal_youth_backend.document.type.enums.DocumentTypeCode;
import org.example.tnal_youth_backend.document.type.service.DocumentTypeService;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTypeService documentTypeService;
    private final DocumentMapper documentMapper;
    private final EntityManager entityManager;

    // =========================================================
    // INSTITUTIONAL DOCUMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public DocumentPageResponse getInstitutionalDocuments(
            DocumentFilterRequest filter,
            Pageable pageable
    ) {
        validatePageable(pageable);
        validateDateRange(filter);

        DocumentFilterRequest effectiveFilter =
                filter == null
                        ? DocumentFilterRequest.empty()
                        : filter;

        Set<String> institutionalTypeCodes =
                DocumentTypeCode.institutionalCodeNames();

        Page<Document> documents = documentRepository.findAll(
                DocumentSpecification.institutionalDocuments(
                        effectiveFilter,
                        institutionalTypeCodes
                ),
                pageable
        );

        return documentMapper.toPageResponse(documents);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDetailResponse getInstitutionalDocumentById(
            Long documentId
    ) {
        validateDocumentId(documentId);

        Document document = documentRepository
                .findInstitutionalDetailedById(
                        documentId,
                        DocumentTypeCode.institutionalCodeNames()
                )
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Institutional document was not found with ID: "
                                        + documentId
                        )
                );

        return documentMapper.toDetailResponse(document);
    }

    @Override
    public DocumentDetailResponse createInstitutionalDocument(
            InstitutionalDocumentRequest request,
            Long uploadedById
    ) {
        requireInstitutionalRequest(request);
        requireUploader(uploadedById);

        DocumentType documentType =
                documentTypeService.requireActiveDocumentType(
                        request.typeId(),
                        DocumentScope.INSTITUTIONAL
                );

        DocumentTypeCode typeCode =
                requireSupportedTypeCode(documentType);

        validateInstitutionalOwnership(
                typeCode,
                request.branchId(),
                request.activityId()
        );

        requireFile(request.fileId());

        validateFileIsNotAlreadyUsed(
                null,
                request.fileId()
        );

        validateInstitutionalReferences(
                typeCode,
                request.branchId(),
                request.activityId()
        );

        Document document =
                documentMapper.toInstitutionalEntity(
                        request,
                        uploadedById
                );

        Document savedDocument =
                documentRepository.saveAndFlush(document);

        return loadDetailedResponse(savedDocument.getId());
    }

    @Override
    public DocumentDetailResponse updateInstitutionalDocument(
            Long documentId,
            InstitutionalDocumentRequest request
    ) {
        validateDocumentId(documentId);
        requireInstitutionalRequest(request);

        Document existingDocument =
                documentRepository
                        .findInstitutionalDetailedById(
                                documentId,
                                DocumentTypeCode.institutionalCodeNames()
                        )
                        .orElseThrow(
                                () -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Institutional document was not found with ID: "
                                                + documentId
                                )
                        );

        DocumentType documentType =
                documentTypeService.requireActiveDocumentType(
                        request.typeId(),
                        DocumentScope.INSTITUTIONAL
                );

        DocumentTypeCode typeCode =
                requireSupportedTypeCode(documentType);

        validateInstitutionalOwnership(
                typeCode,
                request.branchId(),
                request.activityId()
        );

        requireFile(request.fileId());

        validateFileIsNotAlreadyUsed(
                existingDocument.getId(),
                request.fileId()
        );

        validateInstitutionalReferences(
                typeCode,
                request.branchId(),
                request.activityId()
        );

        documentMapper.updateInstitutionalEntity(
                existingDocument,
                request
        );

        documentRepository.saveAndFlush(existingDocument);

        return loadDetailedResponse(existingDocument.getId());
    }

    @Override
    public void deleteInstitutionalDocument(
            Long documentId
    ) {
        validateDocumentId(documentId);

        Document document = documentRepository
                .findInstitutionalDetailedById(
                        documentId,
                        DocumentTypeCode.institutionalCodeNames()
                )
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Institutional document was not found with ID: "
                                        + documentId
                        )
                );

        documentRepository.delete(document);
        documentRepository.flush();
    }

    // =========================================================
    // ALL MEMBER DOCUMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public DocumentPageResponse getMemberDocuments(
            DocumentFilterRequest filter,
            Pageable pageable
    ) {
        validatePageable(pageable);
        validateDateRange(filter);

        DocumentFilterRequest effectiveFilter =
                filter == null
                        ? DocumentFilterRequest.empty()
                        : filter;

        Page<Document> documents = documentRepository.findAll(
                DocumentSpecification.memberDocuments(
                        effectiveFilter,
                        DocumentTypeCode.memberCodeNames()
                ),
                pageable
        );

        return documentMapper.toPageResponse(documents);
    }

    // =========================================================
    // ONE MEMBER'S DOCUMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public DocumentPageResponse getMemberDocumentsByMemberId(
            Long memberId,
            DocumentFilterRequest filter,
            Pageable pageable
    ) {
        validateMemberId(memberId);
        validatePageable(pageable);
        validateDateRange(filter);

        requireMember(memberId);

        DocumentFilterRequest effectiveFilter =
                filter == null
                        ? DocumentFilterRequest.empty()
                        : filter;

        Page<Document> documents = documentRepository.findAll(
                DocumentSpecification.memberDocumentsByMemberId(
                        memberId,
                        effectiveFilter,
                        DocumentTypeCode.memberCodeNames()
                ),
                pageable
        );

        return documentMapper.toPageResponse(documents);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDetailResponse getMemberDocumentById(
            Long memberId,
            Long documentId
    ) {
        validateMemberId(memberId);
        validateDocumentId(documentId);

        Document document = documentRepository
                .findMemberDetailedById(
                        memberId,
                        documentId,
                        DocumentTypeCode.memberCodeNames()
                )
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member document was not found for member ID "
                                        + memberId
                                        + " with document ID "
                                        + documentId
                        )
                );

        return documentMapper.toDetailResponse(document);
    }

    @Override
    public DocumentDetailResponse createMemberDocument(
            Long memberId,
            MemberDocumentRequest request
    ) {
        validateMemberId(memberId);
        requireMemberRequest(request);
        requireMember(memberId);

        Long uploadedById = requireCurrentUploaderId();

        DocumentType documentType =
                documentTypeService.requireActiveDocumentType(
                        request.typeId(),
                        DocumentScope.MEMBER
                );

        DocumentTypeCode typeCode =
                requireSupportedTypeCode(documentType);

        requireFile(request.fileId());

        validateFileIsNotAlreadyUsed(
                null,
                request.fileId()
        );

        validateMemberDocumentOwnership(
                typeCode,
                request.activityId()
        );

        validateMemberDocumentReferences(
                typeCode,
                request.activityId()
        );

        validateMemberCardUniqueness(
                memberId,
                null,
                typeCode
        );

        Document document =
                documentMapper.toMemberEntity(
                        memberId,
                        request,
                        uploadedById
                );

        Document savedDocument =
                documentRepository.saveAndFlush(document);

        return loadDetailedResponse(savedDocument.getId());
    }

    @Override
    public DocumentDetailResponse updateMemberDocument(
            Long memberId,
            Long documentId,
            MemberDocumentRequest request
    ) {
        validateMemberId(memberId);
        validateDocumentId(documentId);
        requireMemberRequest(request);
        requireMember(memberId);

        Document existingDocument =
                documentRepository
                        .findMemberDetailedById(
                                memberId,
                                documentId,
                                DocumentTypeCode.memberCodeNames()
                        )
                        .orElseThrow(
                                () -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Member document was not found for member ID "
                                                + memberId
                                                + " with document ID "
                                                + documentId
                                )
                        );

        DocumentType documentType =
                documentTypeService.requireActiveDocumentType(
                        request.typeId(),
                        DocumentScope.MEMBER
                );

        DocumentTypeCode typeCode =
                requireSupportedTypeCode(documentType);

        requireFile(request.fileId());

        validateFileIsNotAlreadyUsed(
                existingDocument.getId(),
                request.fileId()
        );

        validateMemberDocumentOwnership(
                typeCode,
                request.activityId()
        );

        validateMemberDocumentReferences(
                typeCode,
                request.activityId()
        );

        validateMemberCardUniqueness(
                memberId,
                existingDocument,
                typeCode
        );

        documentMapper.updateMemberEntity(
                existingDocument,
                memberId,
                request
        );

        documentRepository.saveAndFlush(existingDocument);

        return loadDetailedResponse(existingDocument.getId());
    }

    @Override
    public void deleteMemberDocument(
            Long memberId,
            Long documentId
    ) {
        validateMemberId(memberId);
        validateDocumentId(documentId);

        Document document = documentRepository
                .findMemberDetailedById(
                        memberId,
                        documentId,
                        DocumentTypeCode.memberCodeNames()
                )
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member document was not found for member ID "
                                        + memberId
                                        + " with document ID "
                                        + documentId
                        )
                );

        documentRepository.delete(document);
        documentRepository.flush();
    }

    // =========================================================
    // INSTITUTIONAL VALIDATION
    // =========================================================

    private void validateInstitutionalOwnership(
            DocumentTypeCode typeCode,
            Long branchId,
            Long activityId
    ) {
        switch (typeCode) {
            case ORGANIZATION_DOCUMENT -> {
                if (branchId != null || activityId != null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Organization document must not contain branch_id or activity_id"
                    );
                }
            }

            case BRANCH_DOCUMENT -> {
                if (branchId == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "branch_id is required for a branch document"
                    );
                }

                if (activityId != null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Branch document must not contain activity_id"
                    );
                }
            }

            case ACTIVITY_DOCUMENT -> {
                if (activityId == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "activity_id is required for an activity document"
                    );
                }

                if (branchId != null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Activity document must not contain branch_id"
                    );
                }
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported institutional document type: "
                            + typeCode.name()
            );
        }
    }

    private void validateInstitutionalReferences(
            DocumentTypeCode typeCode,
            Long branchId,
            Long activityId
    ) {
        if (typeCode == DocumentTypeCode.BRANCH_DOCUMENT) {
            requireBranch(branchId);
        }

        if (typeCode == DocumentTypeCode.ACTIVITY_DOCUMENT) {
            requireActivity(activityId);
        }
    }

    // =========================================================
    // MEMBER DOCUMENT VALIDATION
    // =========================================================

    private void validateMemberDocumentOwnership(
            DocumentTypeCode typeCode,
            Long activityId
    ) {
        switch (typeCode) {
            case MEMBER_CARD,
                 MEMBER_CERTIFICATE,
                 MEMBER_DOCUMENT -> {
                if (activityId != null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            typeCode.name()
                                    + " must not contain activity_id"
                    );
                }
            }

            case ACTIVITY_CERTIFICATE -> {
                if (activityId == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "activity_id is required for an activity certificate"
                    );
                }
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported member document type: "
                            + typeCode.name()
            );
        }
    }

    private void validateMemberDocumentReferences(
            DocumentTypeCode typeCode,
            Long activityId
    ) {
        if (typeCode == DocumentTypeCode.ACTIVITY_CERTIFICATE) {
            requireActivity(activityId);
        }
    }

    private void validateMemberCardUniqueness(
            Long memberId,
            Document existingDocument,
            DocumentTypeCode requestedTypeCode
    ) {
        if (requestedTypeCode != DocumentTypeCode.MEMBER_CARD) {
            return;
        }

        boolean alreadyHasMemberCard =
                documentRepository.existsMemberDocumentByTypeCode(
                        memberId,
                        DocumentTypeCode.MEMBER_CARD.name()
                );

        if (!alreadyHasMemberCard) {
            return;
        }

        if (
                existingDocument != null
                        && existingDocument.getTypeCode()
                        == DocumentTypeCode.MEMBER_CARD
        ) {
            return;
        }

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Member ID "
                        + memberId
                        + " already has a member card"
        );
    }

    // =========================================================
    // SHARED VALIDATION
    // =========================================================

    private void validateFileIsNotAlreadyUsed(
            Long currentDocumentId,
            Long requestedFileId
    ) {
        if (currentDocumentId != null) {
            Document currentDocument =
                    documentRepository.findById(currentDocumentId)
                            .orElse(null);

            if (
                    currentDocument != null
                            && requestedFileId.equals(
                            currentDocument.getFileId()
                    )
            ) {
                return;
            }
        }

        if (documentRepository.existsByFileId(requestedFileId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "File ID "
                            + requestedFileId
                            + " is already linked to another document"
            );
        }
    }

    private DocumentTypeCode requireSupportedTypeCode(
            DocumentType documentType
    ) {
        DocumentTypeCode typeCode =
                documentType.getTypeCode();

        if (typeCode == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported document type code: "
                            + documentType.getCode()
            );
        }

        return typeCode;
    }

    private DocumentDetailResponse loadDetailedResponse(
            Long documentId
    ) {
        Document document = documentRepository
                .findDetailedById(documentId)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Document was saved but could not be loaded"
                        )
                );

        return documentMapper.toDetailResponse(document);
    }

    private void requireInstitutionalRequest(
            InstitutionalDocumentRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Institutional document request is required"
            );
        }
    }

    private void requireMemberRequest(
            MemberDocumentRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member document request is required"
            );
        }
    }

    private void validateDateRange(
            DocumentFilterRequest filter
    ) {
        if (
                filter != null
                        && !filter.isDateRangeValid()
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "date_from must not be after date_to"
            );
        }
    }

    private void validatePageable(
            Pageable pageable
    ) {
        if (pageable == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Pagination information is required"
            );
        }
    }

    private void validateDocumentId(
            Long documentId
    ) {
        validatePositiveId(
                documentId,
                "Document ID"
        );
    }

    private void validateMemberId(
            Long memberId
    ) {
        validatePositiveId(
                memberId,
                "Member ID"
        );
    }

    private void validatePositiveId(
            Long id,
            String fieldName
    ) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }

        if (id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be positive"
            );
        }
    }

    // =========================================================
    // REFERENCE VALIDATION
    // =========================================================

    private void requireMember(
            Long memberId
    ) {
        requireEntity(
                Member.class,
                memberId,
                "Member"
        );
    }

    private void requireBranch(
            Long branchId
    ) {
        requireEntity(
                Branch.class,
                branchId,
                "Branch"
        );
    }

    private void requireActivity(
            Long activityId
    ) {
        requireEntity(
                Activity.class,
                activityId,
                "Activity"
        );
    }

    private void requireFile(
            Long fileId
    ) {
        requireEntity(
                FileEntity.class,
                fileId,
                "File"
        );
    }

    private void requireUploader(
            Long uploadedById
    ) {
        requireEntity(
                User.class,
                uploadedById,
                "Uploader"
        );
    }

    private Long requireCurrentUploaderId() {
        User authenticatedUser = SecurityUtil.getCurrentUser();

        if (authenticatedUser == null
                || authenticatedUser.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user was not found"
            );
        }

        Long uploadedById = authenticatedUser.getId();

        requireEntity(
                User.class,
                uploadedById,
                "Uploader"
        );

        return uploadedById;
    }

    private <T> void requireEntity(
            Class<T> entityClass,
            Long entityId,
            String entityName
    ) {
        validatePositiveId(
                entityId,
                entityName + " ID"
        );

        T entity = entityManager.find(
                entityClass,
                entityId
        );

        if (entity == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    entityName
                            + " was not found with ID: "
                            + entityId
            );
        }
    }

}