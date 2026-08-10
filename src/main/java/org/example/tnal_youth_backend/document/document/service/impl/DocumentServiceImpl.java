package org.example.tnal_youth_backend.document.document.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.document.document.dto.request.DocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentResponse;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.example.tnal_youth_backend.document.document.mapper.DocumentMapper;
import org.example.tnal_youth_backend.document.document.repository.DocumentRepository;
import org.example.tnal_youth_backend.document.document.service.DocumentService;
import org.example.tnal_youth_backend.document.document.service.DocumentAccessPolicy;
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
import java.util.Locale;

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
    private final DocumentMapper documentMapper;
    private final DocumentAccessPolicy documentAccessPolicy;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(
            String ownerType,
            Long ownerId,
            Short typeId,
            String search
    ) {
        String normalizedOwnerType = normalizeOwnerType(ownerType);
        validateOwnerFilter(normalizedOwnerType, ownerId);
        User currentUser = documentAccessPolicy.currentUser();
        String normalizedSearch = trimToNull(search);

        return documentRepository
                .findAllByOrderByCreatedAtDescIdDesc()
                .stream()
                .filter(document -> documentAccessPolicy.canAccess(currentUser, document))
                .filter(document -> matchesOwner(document, normalizedOwnerType, ownerId))
                .filter(document -> typeId == null || typeId.equals(document.getTypeId()))
                .filter(document -> matchesSearch(document, normalizedSearch))
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(
            Long id
    ) {
        Document document = findDocument(id);
        documentAccessPolicy.requireAccess(documentAccessPolicy.currentUser(), document);
        return documentMapper.toResponse(document);
    }

    @Override
    @Transactional
    public DocumentResponse createDocument(
            DocumentRequest request
    ) {
        validateRequest(request);
        User currentUser = documentAccessPolicy.currentUser();
        documentAccessPolicy.requireOwnerAccess(
                currentUser,
                request.branchId(),
                request.memberId(),
                request.activityId()
        );

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
                .uploadedById(currentUser.getId())
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
        User currentUser = documentAccessPolicy.currentUser();
        documentAccessPolicy.requireAccess(currentUser, document);

        validateRequest(request);
        documentAccessPolicy.requireOwnerAccess(
                currentUser,
                request.branchId(),
                request.memberId(),
                request.activityId()
        );

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
        documentAccessPolicy.requireAccess(documentAccessPolicy.currentUser(), document);

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
                && documentTypeRepository.findById(request.typeId())
                .filter(type -> Boolean.TRUE.equals(type.getIsActive()))
                .isEmpty()) {

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

    }

    private String normalizeOwnerType(String ownerType) {
        String normalized = trimToNull(ownerType);
        if (normalized == null) {
            return null;
        }

        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!normalized.equals("BRANCH")
                && !normalized.equals("MEMBER")
                && !normalized.equals("ACTIVITY")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "owner_type must be BRANCH, MEMBER, or ACTIVITY"
            );
        }
        return normalized;
    }

    private void validateOwnerFilter(String ownerType, Long ownerId) {
        if (ownerId != null && ownerId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "owner_id must be positive");
        }
        if (ownerId != null && ownerType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "owner_type is required with owner_id");
        }
    }

    private boolean matchesOwner(Document document, String ownerType, Long ownerId) {
        if (ownerType == null) {
            return true;
        }

        Long documentOwnerId = switch (ownerType) {
            case "BRANCH" -> document.getBranchId();
            case "MEMBER" -> document.getMemberId();
            case "ACTIVITY" -> document.getActivityId();
            default -> null;
        };
        return documentOwnerId != null && (ownerId == null || ownerId.equals(documentOwnerId));
    }

    private boolean matchesSearch(Document document, String search) {
        if (search == null) {
            return true;
        }

        String needle = search.toLowerCase(Locale.ROOT);
        return contains(document.getTitle(), needle)
                || contains(document.getDescription(), needle)
                || (document.getFile() != null && contains(document.getFile().getOriginalName(), needle))
                || (document.getBranch() != null
                    && (contains(document.getBranch().getNameKm(), needle)
                    || contains(document.getBranch().getNameEn(), needle)))
                || (document.getMember() != null
                    && (contains(document.getMember().getFullNameKm(), needle)
                    || contains(document.getMember().getFullNameEn(), needle)
                    || contains(document.getMember().getMemberNo(), needle)));
    }

    private boolean contains(String value, String lowerCaseNeedle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerCaseNeedle);
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
