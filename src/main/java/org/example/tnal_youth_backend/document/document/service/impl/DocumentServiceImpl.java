package org.example.tnal_youth_backend.document.document.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.document.document.dto.request.DocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentResponse;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.example.tnal_youth_backend.document.document.mapper.DocumentMapper;
import org.example.tnal_youth_backend.document.document.repository.DocumentRepository;
import org.example.tnal_youth_backend.document.document.service.DocumentService;
import org.example.tnal_youth_backend.document.type.repository.DocumentTypeRepository;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl
        implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final FileRepository fileRepository;
    private final BranchRepository branchRepository;
    private final MemberRepository memberRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository
                .findAllByOrderByCreatedAtDescIdDesc()
                .stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(
            Long id
    ) {
        return documentMapper.toResponse(
                findDocument(id)
        );
    }

    @Override
    @Transactional
    public DocumentResponse createDocument(
            DocumentRequest request
    ) {
        validateRequest(request);

        Document document = Document.builder()
                .typeId(request.typeId())
                .fileId(request.fileId())
                .title(
                        normalizeRequired(
                                request.title()
                        )
                )
                .description(
                        trimToNull(request.description())
                )
                .branchId(request.branchId())
                .memberId(request.memberId())
                .activityId(request.activityId())
                .uploadedById(request.uploadedById())
                .build();

        try {
            Document saved =
                    documentRepository.saveAndFlush(document);

            return documentMapper.toResponse(saved);

        } catch (DataIntegrityViolationException exception) {
            throw databaseConstraintException(exception);
        }
    }

    @Override
    @Transactional
    public DocumentResponse updateDocument(
            Long id,
            DocumentRequest request
    ) {
        Document document = findDocument(id);

        validateRequest(request);

        document.setTypeId(request.typeId());
        document.setFileId(request.fileId());

        document.setTitle(
                normalizeRequired(request.title())
        );

        document.setDescription(
                trimToNull(request.description())
        );

        document.setBranchId(request.branchId());
        document.setMemberId(request.memberId());
        document.setActivityId(request.activityId());
        document.setUploadedById(request.uploadedById());

        try {
            Document updated =
                    documentRepository.saveAndFlush(document);

            return documentMapper.toResponse(updated);

        } catch (DataIntegrityViolationException exception) {
            throw databaseConstraintException(exception);
        }
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        Document document = findDocument(id);

        try {
            documentRepository.delete(document);
            documentRepository.flush();

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    """
                    Cannot delete this document because another \
                    record still references it.
                    """
            );
        }
    }

    private Document findDocument(Long id) {
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document ID is required"
            );
        }

        return documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Document not found with ID: " + id
                        )
                );
    }

    private void validateRequest(
            DocumentRequest request
    ) {
        validateOwnerSelection(request);

        if (request.typeId() != null
                && !documentTypeRepository.existsById(
                request.typeId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Document type not found with ID: "
                            + request.typeId()
            );
        }

        if (!fileRepository.existsById(
                request.fileId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "File not found with ID: "
                            + request.fileId()
            );
        }

        if (request.branchId() != null
                && !branchRepository.existsById(
                request.branchId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Branch not found with ID: "
                            + request.branchId()
            );
        }

        if (request.memberId() != null
                && !memberRepository.existsById(
                request.memberId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Member not found with ID: "
                            + request.memberId()
            );
        }

        if (request.activityId() != null
                && !activityRepository.existsById(
                request.activityId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Activity not found with ID: "
                            + request.activityId()
            );
        }

        if (request.uploadedById() != null
                && !userRepository.existsById(
                request.uploadedById()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Uploader not found with ID: "
                            + request.uploadedById()
            );
        }
    }

    private void validateOwnerSelection(
            DocumentRequest request
    ) {
        int ownerCount = 0;

        if (request.branchId() != null) {
            ownerCount++;
        }

        if (request.memberId() != null) {
            ownerCount++;
        }

        if (request.activityId() != null) {
            ownerCount++;
        }

        if (ownerCount != 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    """
                    Exactly one owner must be provided: branch_id, \
                    member_id, or activity_id
                    """
            );
        }
    }

    private String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document title is required"
            );
        }

        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }

    private ResponseStatusException
    databaseConstraintException(
            DataIntegrityViolationException exception
    ) {
        String databaseMessage =
                exception.getMostSpecificCause()
                        .getMessage();

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                """
                Document could not be saved. Check that type_id, \
                file_id, branch_id, member_id, activity_id, and \
                uploaded_by reference existing records. Exactly \
                one owner must be provided.

                Database message: %s
                """.formatted(databaseMessage)
        );
    }
}