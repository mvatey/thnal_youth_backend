package org.example.tnal_youth_backend.document.document.service.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
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
import org.example.tnal_youth_backend.document.type.enums.DocumentTypeCode;
import org.example.tnal_youth_backend.document.type.service.DocumentTypeService;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.file.service.FileService;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    /*
     * The upload UI states that document files must not exceed 5 MB.
     */
    private static final long MAX_FILE_SIZE_BYTES =
            5L * 1024L * 1024L;

    /*
     * File types accepted by both Create UI flows:
     *
     * 1. Generated card, letter or certificate image.
     * 2. Uploaded member personal document.
     */
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "pdf",
                    "docx",
                    "jpg",
                    "jpeg",
                    "png",
                    "webp"
            );

    private static final Set<String> IMAGE_EXTENSIONS =
            Set.of(
                    "jpg",
                    "jpeg",
                    "png",
                    "webp"
            );

    private final DocumentRepository documentRepository;
    private final DocumentTypeService documentTypeService;
    private final DocumentMapper documentMapper;
    private final FileService fileService;
    private final EntityManager entityManager;

    // =========================================================
    // INSTITUTIONAL DOCUMENTS
    // =========================================================

    /**
     * Returns only branch-owned institutional documents.
     */
    @Override
    @Transactional(readOnly = true)
    public DocumentPageResponse getInstitutionalDocuments(
            DocumentFilterRequest filter,
            Pageable pageable
    ) {
        if (pageable == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Pagination information is required"
            );
        }

        if (filter != null && !filter.isDateRangeValid()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "date_from must not be after date_to"
            );
        }

        DocumentFilterRequest effectiveFilter =
                filter == null
                        ? DocumentFilterRequest.empty()
                        : filter;

        Page<Document> documents =
                documentRepository.findAll(
                        DocumentSpecification.institutionalDocuments(
                                effectiveFilter
                        ),
                        pageable
                );

        return documentMapper.toPageResponse(
                documents
        );
    }

    /**
     * Returns one branch-owned institutional document.
     */
    @Override
    @Transactional(readOnly = true)
    public DocumentDetailResponse getInstitutionalDocumentById(
            Long documentId
    ) {
        validatePositiveId(
                documentId,
                "Document ID"
        );

        Document document =
                requireInstitutionalDocument(
                        documentId
                );

        return documentMapper.toDetailResponse(
                document
        );
    }

    /**
     * Creates a BRANCH_DOCUMENT from the Institutional Document UI.
     */
    @Override
    public DocumentDetailResponse createInstitutionalDocument(
            InstitutionalDocumentRequest.Create request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Institutional document request is required"
            );
        }

        requireEntity(
                Branch.class,
                request.branchId(),
                "Branch"
        );

        Long uploadedById =
                requireCurrentUploaderId();

        DocumentType branchDocumentType =
                documentTypeService.requireActiveDocumentType(
                        DocumentTypeCode.BRANCH_DOCUMENT
                );

        FileEntity uploadedFile =
                uploadFile(
                        request.file(),
                        uploadedById
                );

        try {
            validateFileIsNotAlreadyUsed(
                    null,
                    uploadedFile.getId()
            );

            Document document =
                    documentMapper.toInstitutionalEntity(
                            branchDocumentType.getId(),
                            uploadedFile.getId(),
                            uploadedById,
                            request
                    );

            Document savedDocument =
                    documentRepository.saveAndFlush(
                            document
                    );

            return loadDetailedResponse(
                    savedDocument.getId()
            );

        } catch (RuntimeException exception) {
            /*
             * If saving the document fails after the file was uploaded,
             * remove the unused uploaded file.
             */
            safelyDeleteFile(
                    uploadedFile.getId()
            );

            throw exception;
        }
    }

    /**
     * Updates a branch-owned institutional document.
     *
     * When no replacement file is provided, the existing file is preserved.
     */
    @Override
    public DocumentDetailResponse updateInstitutionalDocument(
            Long documentId,
            InstitutionalDocumentRequest.Update request
    ) {
        validatePositiveId(
                documentId,
                "Document ID"
        );

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Institutional document request is required"
            );
        }

        Document existingDocument =
                requireInstitutionalDocument(
                        documentId
                );

        requireEntity(
                Branch.class,
                request.branchId(),
                "Branch"
        );

        DocumentType branchDocumentType =
                documentTypeService.requireActiveDocumentType(
                        DocumentTypeCode.BRANCH_DOCUMENT
                );

        Long previousFileId =
                existingDocument.getFileId();

        Long nextFileId =
                previousFileId;

        Long newlyUploadedFileId =
                null;

        MultipartFile replacementFile =
                request.file();

        if (
                replacementFile != null
                        && !replacementFile.isEmpty()
        ) {
            Long uploadedById =
                    requireCurrentUploaderId();

            FileEntity uploadedFile =
                    uploadFile(
                            replacementFile,
                            uploadedById
                    );

            newlyUploadedFileId =
                    uploadedFile.getId();

            nextFileId =
                    newlyUploadedFileId;

            validateFileIsNotAlreadyUsed(
                    existingDocument.getId(),
                    nextFileId
            );
        }

        try {
            documentMapper.updateInstitutionalEntity(
                    existingDocument,
                    branchDocumentType.getId(),
                    nextFileId,
                    request
            );

            documentRepository.saveAndFlush(
                    existingDocument
            );

            DocumentDetailResponse response =
                    loadDetailedResponse(
                            existingDocument.getId()
                    );

            /*
             * Delete the previous file only after the document update
             * was saved and reloaded successfully.
             */
            if (
                    previousFileId != null
                            && nextFileId != null
                            && !previousFileId.equals(nextFileId)
            ) {
                safelyDeleteFile(
                        previousFileId
                );
            }

            return response;

        } catch (RuntimeException exception) {
            /*
             * If the update fails, remove only the newly uploaded file.
             * The previous file remains linked to the current document.
             */
            if (
                    newlyUploadedFileId != null
                            && !newlyUploadedFileId.equals(previousFileId)
            ) {
                safelyDeleteFile(
                        newlyUploadedFileId
                );
            }

            throw exception;
        }
    }

    /**
     * Deletes the branch institutional document and its uploaded file.
     */
    @Override
    public void deleteInstitutionalDocument(
            Long documentId
    ) {
        validatePositiveId(
                documentId,
                "Document ID"
        );

        Document document =
                requireInstitutionalDocument(
                        documentId
                );

        Long fileId =
                document.getFileId();

        documentRepository.delete(
                document
        );

        documentRepository.flush();

        if (fileId != null) {
            safelyDeleteFile(
                    fileId
            );
        }
    }

    // =========================================================
    // MEMBER PERSONAL DOCUMENTS
    // =========================================================

    /**
     * Returns all member-owned personal documents.
     *
     * Member ownership rules:
     *
     * member_id is not null
     * branch_id is null
     * activity_id is null
     */
    @Override
    @Transactional(readOnly = true)
    public DocumentPageResponse getMemberDocuments(
            DocumentFilterRequest filter,
            Pageable pageable
    ) {
        if (pageable == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Pagination information is required"
            );
        }

        if (filter != null && !filter.isDateRangeValid()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "date_from must not be after date_to"
            );
        }

        DocumentFilterRequest effectiveFilter =
                filter == null
                        ? DocumentFilterRequest.empty()
                        : filter;

        Page<Document> documents =
                documentRepository.findAll(
                        DocumentSpecification.memberDocuments(
                                effectiveFilter
                        ),
                        pageable
                );

        return documentMapper.toPageResponse(
                documents
        );
    }

    /**
     * Returns one member-owned personal document.
     */
    @Override
    @Transactional(readOnly = true)
    public DocumentDetailResponse getMemberDocumentById(
            Long documentId
    ) {
        validatePositiveId(
                documentId,
                "Document ID"
        );

        Document document =
                requireMemberDocument(
                        documentId
                );

        return documentMapper.toDetailResponse(
                document
        );
    }

    /**
     * Creates a new document belonging to a selected member.
     *
     * This operation supports both Member Document Create screens:
     *
     * 1. A generated member card, letter or certificate image.
     * 2. A personal document uploaded from the user's device.
     *
     * The frontend branch selector is only used to filter members.
     * It is not stored in documents.branch_id.
     */
    @Override
    public DocumentDetailResponse createMemberDocument(
            MemberDocumentRequest.Create request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Member document request is required"
            );
        }

        /*
         * Confirm that the selected member exists.
         */
        requireEntity(
                Member.class,
                request.memberId(),
                "Member"
        );

        /*
         * Only allow types supported by the current Create UI:
         *
         * MEMBER_CARD
         * MEMBER_LETTER
         * MEMBER_CERTIFICATE
         * MEMBER_DOCUMENT
         */
        DocumentType memberDocumentType =
                requireMemberPersonalCreateDocumentType(
                        request.typeId()
                );

        Long uploadedById =
                requireCurrentUploaderId();

        /*
         * Upload the generated or manually selected file.
         */
        FileEntity uploadedFile =
                uploadFile(
                        request.file(),
                        uploadedById
                );

        try {
            validateFileIsNotAlreadyUsed(
                    null,
                    uploadedFile.getId()
            );

            /*
             * The mapper creates:
             *
             * member_id   = selected member
             * branch_id   = null
             * activity_id = null
             */
            Document document =
                    documentMapper.toMemberEntity(
                            memberDocumentType.getId(),
                            uploadedFile.getId(),
                            uploadedById,
                            request
                    );

            Document savedDocument =
                    documentRepository.saveAndFlush(
                            document
                    );

            return loadDetailedResponse(
                    savedDocument.getId()
            );

        } catch (RuntimeException exception) {
            /*
             * If the file upload succeeded but document creation failed,
             * delete the unused uploaded file.
             */
            safelyDeleteFile(
                    uploadedFile.getId()
            );

            throw exception;
        }
    }

    // =========================================================
    // DOCUMENT LOADERS
    // =========================================================

    /**
     * Loads only documents satisfying the branch institutional ownership
     * rules defined by the repository.
     */
    private Document requireInstitutionalDocument(
            Long documentId
    ) {
        return documentRepository
                .findInstitutionalDetailedById(
                        documentId
                )
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Institutional document was not found with ID: "
                                        + documentId
                        )
                );
    }

    /**
     * Loads only a member-owned personal document.
     */
    private Document requireMemberDocument(
            Long documentId
    ) {
        return documentRepository
                .findMemberDetailedById(
                        documentId
                )
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Member document was not found with ID: "
                                        + documentId
                        )
                );
    }

    // =========================================================
    // MEMBER DOCUMENT TYPE VALIDATION
    // =========================================================

    /**
     * Validates that the selected type can be created from the current
     * Member Personal Document Create screens.
     *
     * Supported:
     *
     * MEMBER_CARD
     * MEMBER_LETTER
     * MEMBER_CERTIFICATE
     * MEMBER_DOCUMENT
     *
     * ACTIVITY_CERTIFICATE is rejected because it requires activityId and is
     * not part of the current Create UI.
     */
    private DocumentType requireMemberPersonalCreateDocumentType(
            Short typeId
    ) {
        if (typeId == null || typeId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document type ID must be positive"
            );
        }

        DocumentType documentType =
                entityManager.find(
                        DocumentType.class,
                        typeId
                );

        if (documentType == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Document type was not found with ID: "
                            + typeId
            );
        }

        DocumentTypeCode typeCode =
                documentType.getTypeCode();

        if (typeCode == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The selected document type has an unsupported code"
            );
        }

        if (!typeCode.isMemberPersonalCreateType()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported document type for Member Document Create: "
                            + typeCode.name()
            );
        }

        return documentType;
    }

    // =========================================================
    // SHARED FILE HELPERS
    // =========================================================

    /**
     * Uploads images through uploadImage and other document files through
     * uploadAttachment.
     */
    private FileEntity uploadFile(
            MultipartFile file,
            Long uploadedById
    ) {
        validateFile(
                file
        );

        String extension =
                extensionOf(
                        file.getOriginalFilename()
                );

        if (IMAGE_EXTENSIONS.contains(extension)) {
            return fileService.uploadImage(
                    file,
                    uploadedById
            );
        }

        return fileService.uploadAttachment(
                file,
                uploadedById
        );
    }

    /**
     * Validates the uploaded file against size and extension rules.
     */
    private void validateFile(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document file is required"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Document file must not exceed 5 MB"
            );
        }

        String extension =
                extensionOf(
                        file.getOriginalFilename()
                );

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported document file type. Allowed: "
                            + "PDF, DOCX, JPG, JPEG, PNG, WEBP"
            );
        }
    }

    /**
     * Extracts and normalizes a file extension.
     */
    private String extensionOf(
            String filename
    ) {
        if (filename == null || filename.isBlank()) {
            return "";
        }

        int dotIndex =
                filename.lastIndexOf('.');

        if (
                dotIndex < 0
                        || dotIndex == filename.length() - 1
        ) {
            return "";
        }

        return filename
                .substring(dotIndex + 1)
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Checks that the uploaded file is not linked to another document.
     */
    private void validateFileIsNotAlreadyUsed(
            Long currentDocumentId,
            Long fileId
    ) {
        validatePositiveId(
                fileId,
                "File ID"
        );

        boolean alreadyUsed;

        if (currentDocumentId == null) {
            alreadyUsed =
                    documentRepository.existsByFileId(
                            fileId
                    );

        } else {
            alreadyUsed =
                    documentRepository.existsByFileIdAndIdNot(
                            fileId,
                            currentDocumentId
                    );
        }

        if (alreadyUsed) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "File ID "
                            + fileId
                            + " is already linked to another document"
            );
        }
    }

    // =========================================================
    // RESPONSE LOADING
    // =========================================================

    /**
     * Reloads all document relationships before mapping the response.
     */
    private DocumentDetailResponse loadDetailedResponse(
            Long documentId
    ) {
        Document document =
                documentRepository
                        .findDetailedById(
                                documentId
                        )
                        .orElseThrow(
                                () -> new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                        "Document was saved but could not be loaded"
                                )
                        );

        return documentMapper.toDetailResponse(
                document
        );
    }

    // =========================================================
    // AUTHENTICATED UPLOADER
    // =========================================================

    /**
     * Returns the authenticated user's database ID.
     */
    private Long requireCurrentUploaderId() {
        User currentUser =
                SecurityUtil.getCurrentUser();

        if (
                currentUser == null
                        || currentUser.getId() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user was not found"
            );
        }

        requireEntity(
                User.class,
                currentUser.getId(),
                "Uploader"
        );

        return currentUser.getId();
    }

    // =========================================================
    // SHARED ENTITY VALIDATION
    // =========================================================

    /**
     * Validates that an entity exists.
     */
    private <T> void requireEntity(
            Class<T> entityClass,
            Long id,
            String entityName
    ) {
        validatePositiveId(
                id,
                entityName + " ID"
        );

        if (entityManager.find(entityClass, id) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    entityName
                            + " was not found with ID: "
                            + id
            );
        }
    }

    /**
     * Validates that an ID is present and positive.
     */
    private void validatePositiveId(
            Long id,
            String fieldName
    ) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be positive"
            );
        }
    }

    // =========================================================
    // FILE CLEANUP
    // =========================================================

    /**
     * File cleanup should not replace the original document operation with a
     * secondary cleanup exception.
     */
    private void safelyDeleteFile(
            Long fileId
    ) {
        if (fileId == null) {
            return;
        }

        try {
            fileService.deleteFile(
                    fileId
            );

        } catch (RuntimeException ignored) {
            /*
             * The document operation remains authoritative.
             */
        }
    }
}