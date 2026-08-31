package org.example.tnal_youth_backend.document.document.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tnal_youth_backend.activity.repository.ActivityRepository;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.document.document.dto.request.DocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.*;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.example.tnal_youth_backend.document.document.mapper.DocumentMapper;
import org.example.tnal_youth_backend.document.document.repository.DocumentRepository;
import org.example.tnal_youth_backend.document.document.service.DocumentService;
import org.example.tnal_youth_backend.document.type.entity.DocumentType;
import org.example.tnal_youth_backend.document.type.repository.DocumentTypeRepository;
import org.example.tnal_youth_backend.file.repository.FileRepository;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.branch.service.BranchService;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.notification.dto.NotificationCreateDTO;
import org.example.tnal_youth_backend.notification.service.NotificationService;
import org.example.tnal_youth_backend.security.SecurityUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl
        implements DocumentService {

    /**
     * Fires only from {@link #createDocument}, never {@link #updateDocument}
     * — the notification feature's trigger #3 was scoped to "the secretary
     * manually created a certificate/letter of appointment for members,"
     * not edits.
     */
    private static final String DOCUMENT_ADDED_TYPE_CODE = "DOCUMENT_ADDED";

    private final DocumentRepository documentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final FileRepository fileRepository;
    private final BranchRepository branchRepository;
    private final MemberRepository memberRepository;
    private final ActivityRepository activityRepository;
    private final DocumentMapper documentMapper;
    private final BranchService branchService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

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
    public List<DocumentResponse> getDocumentsByMemberId(Long memberId) {
        if (memberId == null || !memberRepository.existsById(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Member not found with ID: " + memberId
            );
        }

        return documentRepository
                .findAllByMemberIdOrderByCreatedAtDescIdDesc(memberId)
                .stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(
            Long id
    ) {
        Document document =
                findDocument(id);

        validateExistingDocumentAccess(
                document
        );

        return documentMapper.toResponse(
                document
        );
    }

    @Override
    @Transactional
    public DocumentResponse createDocument(
            DocumentRequest request
    ) {
        validateRequest(request);

        /*
         * Security:
         * Admin / Secretary / Branch Leader may only create
         * documents inside branches they can access.
         */
        validateDocumentOwnerAccess(request);

        Long currentUserId =
                SecurityUtils.getCurrentUserId();

        Document document =
                Document.builder()
                        .typeId(
                                request.typeId()
                        )
                        .fileId(
                                request.fileId()
                        )
                        .title(
                                normalizeRequired(
                                        request.title()
                                )
                        )
                        .description(
                                trimToNull(
                                        request.description()
                                )
                        )
                        .branchId(
                                request.branchId()
                        )
                        .memberId(
                                request.memberId()
                        )
                        .activityId(
                                request.activityId()
                        )
                        .uploadedById(
                                currentUserId
                        )
                        .build();

        try {
            Document saved =
                    documentRepository
                            .saveAndFlush(
                                    document
                            );

            if (!Boolean.TRUE.equals(request.suppressNotification())) {
                notifyMemberDocumentIssued(
                        saved
                );
            }

            return documentMapper
                    .toResponse(
                            saved
                    );

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw databaseConstraintException(
                    exception
            );
        }
    }

    /**
     * Notifies the document's owning member — via their linked user
     * account's email/Telegram, same {@link NotificationCreatedEvent}
     * pipeline as the activity triggers — that a document was just
     * issued to them. This is how certificates and letters of
     * appointment reach this hook: both save through the exact same
     * {@code POST /api/documents} member-owned-document path (see
     * {@code document/create/page.js}'s {@code saveMemberCertificatesToBackend}
     * / {@code saveAppointmentLettersToBackend}), so there's no need for
     * a separate certificate/letter-specific endpoint — every
     * member-owned document created here already flows through this one
     * place.
     *
     * <p>Best-effort and entirely swallowed on failure (missing/inactive
     * notification type, no linked user account, a downstream
     * email/Telegram error): none of that should ever fail the actual
     * document save, which is the part the secretary actually asked for.
     */
    private void notifyMemberDocumentIssued(
            Document saved
    ) {
        if (saved.getMemberId() == null) {
            return;
        }

        try {
            User recipientUser =
                    userRepository
                            .findByMemberId(saved.getMemberId())
                            .orElse(null);

            if (recipientUser == null) {
                return;
            }

            Short typeId =
                    notificationService.findActiveTypeIdByCode(
                            DOCUMENT_ADDED_TYPE_CODE
                    );

            if (typeId == null) {
                return;
            }

            NotificationCreateDTO notification =
                    new NotificationCreateDTO();

            notification.setTypeId(typeId);
            notification.setTitle("ឯកសារថ្មីត្រូវបានចេញ");
            notification.setBody(
                    "ឯកសារ \"" + saved.getTitle()
                            + "\" ត្រូវបានចេញជូនអ្នក។ សូមចូលទៅកាន់គណនីរបស់អ្នកដើម្បីមើលឯកសារ។"
            );
            notification.setTitleEn("New Document Issued");
            notification.setBodyEn(
                    "The document \"" + saved.getTitle()
                            + "\" has been issued to you. Please log in to your account to view it."
            );
            notification.setDocumentId(saved.getId());
            notification.setActionUrl("/myAcc/documents");
            notification.setTarget(
                    NotificationCreateDTO.TargetMode.USERS
            );
            notification.setTargetUserIds(
                    List.of(recipientUser.getId())
            );

            notificationService.create(notification);

        } catch (Exception exception) {
            log.warn(
                    "DocumentServiceImpl: failed to notify member {} about document {}",
                    saved.getMemberId(),
                    saved.getId(),
                    exception
            );
        }
    }

    @Override
    @Transactional
    public DocumentResponse updateDocument(
            Long id,
            DocumentRequest request
    ) {
        Document document =
                findDocument(id);

        /*
         * User must already have access to the
         * document being edited.
         */
        validateExistingDocumentAccess(
                document
        );

        validateRequest(
                request
        );

        /*
         * Also validate the new owner.
         * Prevents moving the document to a branch
         * outside the user's scope.
         */
        validateDocumentOwnerAccess(
                request
        );

        document.setTypeId(
                request.typeId()
        );

        document.setFileId(
                request.fileId()
        );

        document.setTitle(
                normalizeRequired(
                        request.title()
                )
        );

        document.setDescription(
                trimToNull(
                        request.description()
                )
        );

        document.setBranchId(
                request.branchId()
        );

        document.setMemberId(
                request.memberId()
        );

        document.setActivityId(
                request.activityId()
        );

        try {
            Document updated =
                    documentRepository
                            .saveAndFlush(
                                    document
                            );

            return documentMapper
                    .toResponse(
                            updated
                    );

        } catch (
                DataIntegrityViolationException exception
        ) {
            throw databaseConstraintException(
                    exception
            );
        }
    }

    @Override
    @Transactional
    public void deleteDocument(
            Long id
    ) {
        Document document =
                findDocument(id);

        validateExistingDocumentAccess(
                document
        );

        try {
            documentRepository.delete(
                    document
            );

            documentRepository.flush();

        } catch (
                DataIntegrityViolationException exception
        ) {
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

    @Override
    @Transactional(readOnly = true)
    public DocumentPageResponse getDocuments(
            int page,
            int size,
            String search,
            Short typeId,
            Long branchId,
            Long memberId,
            Long activityId,
            LocalDate date
    ) {
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must not be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Size must be between 1 and 100"
            );
        }

        String normalizedSearch =
                search == null
                        ? ""
                        : search.trim();

        /*
         * Validate optional filters.
         */
        if (
                typeId != null
                        && !documentTypeRepository.existsById(typeId)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Document type not found with ID: "
                            + typeId
            );
        }

        if (
                branchId != null
                        && !branchRepository.existsById(branchId)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Branch not found with ID: "
                            + branchId
            );
        }

        if (
                memberId != null
                        && !memberRepository.existsById(memberId)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Member not found with ID: "
                            + memberId
            );
        }

        if (
                activityId != null
                        && !activityRepository.existsById(activityId)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Activity not found with ID: "
                            + activityId
            );
        }

        /*
         * Date filter.
         *
         * Cambodia time:
         * selected date 00:00
         * until next date 00:00
         */
        ZoneOffset cambodiaOffset =
                ZoneOffset.ofHours(7);

        /*
         * Always provide non-null values to PostgreSQL.
         *
         * No date selected:
         * use a very wide date range.
         */
        OffsetDateTime startDateTime =
                LocalDate
                        .of(1900, 1, 1)
                        .atStartOfDay()
                        .atOffset(cambodiaOffset);

        OffsetDateTime endDateTime =
                LocalDate
                        .of(9999, 1, 1)
                        .atStartOfDay()
                        .atOffset(cambodiaOffset);

        /*
         * Date selected:
         * restrict to that Cambodia calendar day.
         */
        if (date != null) {
            startDateTime =
                    date
                            .atStartOfDay()
                            .atOffset(cambodiaOffset);

            endDateTime =
                    date
                            .plusDays(1)
                            .atStartOfDay()
                            .atOffset(cambodiaOffset);
        }

        /*
         * Resolve authenticated role.
         */
        User currentUser =
                SecurityUtil.getCurrentUser();

        if (
                currentUser == null
                        || currentUser.getRole() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        UserRole currentRole =
                currentUser.getRole();

        /*
         * VIEWER has the same read authority as ADMIN throughout this
         * app (see UserRole's doc comment) — folded into isAdmin here
         * so every admin-scoped branch below (unrestricted visibility,
         * skip the accessible-branch checks) applies to VIEWER too.
         */
        boolean isAdmin =
                currentRole == UserRole.ADMIN
                        || currentRole == UserRole.VIEWER;

        boolean isMember =
                currentRole == UserRole.MEMBER;

        if (
                !isAdmin
                        && !isMember
                        && currentRole != UserRole.SECRETARY
                        && currentRole != UserRole.BRANCH_LEADER
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to view organizational documents"
            );
        }

        /*
         * A plain MEMBER only sees documents that belong to them
         * personally here — never the branch-wide organizational list
         * below (staff-only), and never an activity's own attachments
         * either (those stay visible only on that activity's own detail
         * page, not mixed into "My Account -> Documents").
         */
        if (isMember) {
            Long selfMemberId = currentUser.getMemberId();

            if (selfMemberId == null) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Your account is not linked to a member record"
                );
            }

            Pageable memberPageable =
                    PageRequest.of(
                            page,
                            size,
                            Sort.by(
                                    Sort.Direction.DESC,
                                    "createdAt"
                            ).and(
                                    Sort.by(
                                            Sort.Direction.DESC,
                                            "id"
                                    )
                            )
                    );

            Page<Document> memberVisible =
                    documentRepository.findVisibleToMemberPage(
                            selfMemberId,
                            normalizedSearch,
                            typeId,
                            startDateTime,
                            endDateTime,
                            memberPageable
                    );

            List<DocumentResponse> memberContent =
                    memberVisible.getContent()
                            .stream()
                            .map(documentMapper::toResponse)
                            .toList();

            return new DocumentPageResponse(
                    memberContent,
                    memberVisible.getNumber(),
                    memberVisible.getSize(),
                    memberVisible.getTotalElements(),
                    memberVisible.getTotalPages(),
                    memberVisible.isFirst(),
                    memberVisible.isLast()
            );
        }

        /*
         * ADMIN:
         * getAccessibleBranchIds() returns all branches.
         *
         * SECRETARY / BRANCH_LEADER:
         * returns only their accessible branches.
         *
         * VIEWER:
         * BranchService's own role check only knows about
         * ADMIN/SECRETARY/BRANCH_LEADER and throws FORBIDDEN for
         * anything else, so VIEWER skips that call entirely rather
         * than being misclassified as branch-scoped staff or blocked
         * outright — isAdmin is already true for VIEWER above, so the
         * query below ignores queryBranchIds' contents for it anyway.
         */
        Set<Long> accessibleBranchIds =
                currentRole == UserRole.VIEWER
                        ? Set.of()
                        : branchService.getAccessibleBranchIds();

        /*
         * Non-admin with no branch access has no documents.
         */
        if (
                !isAdmin
                        && accessibleBranchIds.isEmpty()
        ) {
            return new DocumentPageResponse(
                    List.of(),
                    page,
                    size,
                    0,
                    0,
                    true,
                    true
            );
        }

        /*
         * If frontend explicitly requests branchId,
         * it must also be inside the user's scope.
         */
        if (
                branchId != null
                        && !isAdmin
                        && !accessibleBranchIds.contains(branchId)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "The selected branch is outside your accessible scope"
            );
        }

        /*
         * For ADMIN, avoid problems with an empty IN collection.
         *
         * The query ignores accessibleBranchIds when isAdmin = true,
         * but Hibernate still receives a valid non-empty collection.
         */
        Set<Long> queryBranchIds =
                accessibleBranchIds.isEmpty()
                        ? Set.of(-1L)
                        : accessibleBranchIds;

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        ).and(
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "id"
                                )
                        )
                );

        Page<Document> result =
                documentRepository
                        .findDocumentPage(
                                normalizedSearch,
                                typeId,
                                branchId,
                                memberId,
                                activityId,
                                startDateTime,
                                endDateTime,
                                isAdmin,
                                queryBranchIds,
                                pageable
                        );

        List<DocumentResponse> content =
                result
                        .getContent()
                        .stream()
                        .map(
                                documentMapper::toResponse
                        )
                        .toList();

        return new DocumentPageResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTypeOptionResponse>
    getDocumentTypeOptions() {

        return documentTypeRepository
                .findAllByIsActiveTrueOrderBySortOrderAscIdAsc()
                .stream()
                .map(type ->
                        new DocumentTypeOptionResponse(
                                type.getId(),
                                type.getCode(),
                                type.getLabelKm(),
                                type.getLabelEn()
                        )
                )
                .toList();
    }

    private void validateDocumentOwnerAccess(
            DocumentRequest request
    ) {

        /*
         * Branch-owned document.
         */
        if (request.branchId() != null) {

            branchService
                    .getAccessibleBranchById(
                            request.branchId()
                    );

            return;
        }

        /*
         * Member-owned document.
         *
         * Member itself must exist, then its branch
         * must be accessible to the current user --
         * UNLESS this is a personal activity certificate
         * (certificateActivityId set), in which case the ACTIVITY's own
         * host branch is what's checked instead. This is the same
         * carve-out MemberCredentialServiceImpl.create() applies for the
         * credential this document gets linked to right after: the
         * organizing branch may certify any attendee of its own
         * activity, including a co-hosting branch's member it doesn't
         * otherwise manage.
         */
        if (request.memberId() != null) {

            var member =
                    memberRepository
                            .findById(
                                    request.memberId()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Member not found with ID: "
                                                    + request.memberId()
                                    )
                            );

            if (request.certificateActivityId() != null) {
                Long hostBranchId =
                        activityRepository
                                .findById(request.certificateActivityId())
                                .map(activity -> activity.getBranchId())
                                .orElseThrow(() ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Activity not found with ID: "
                                                        + request.certificateActivityId()
                                        )
                                );

                branchService
                        .getAccessibleBranchById(
                                hostBranchId
                        );

                return;
            }

            branchService
                    .getAccessibleBranchById(
                            member.getBranchId()
                    );

            return;
        }

        /*
         * Activity-owned document.
         */
        if (request.activityId() != null) {

            var activity =
                    activityRepository
                            .findById(
                                    request.activityId()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Activity not found with ID: "
                                                    + request.activityId()
                                    )
                            );

            branchService
                    .getAccessibleBranchById(
                            activity.getBranchId()
                    );
        }
    }

    private void validateExistingDocumentAccess(
            Document document
    ) {
        if (document == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Document was not found"
            );
        }

        /*
         * VIEWER has the same read authority as ADMIN throughout this
         * app (see UserRole's doc comment), but the branchService
         * calls below only know about ADMIN/SECRETARY/BRANCH_LEADER
         * and throw FORBIDDEN for anything else — so VIEWER is
         * special-cased here rather than touching BranchService for
         * this one gap. Safe to short-circuit unconditionally: this
         * method is also reached from updateDocument/deleteDocument,
         * but those are SECRETARY/BRANCH_LEADER-only per
         * DocumentController's @PreAuthorize, so a VIEWER caller can
         * only ever arrive here via the read-only getDocumentById path.
         */
        User currentUser =
                SecurityUtil.getCurrentUser();

        if (
                currentUser != null
                        && currentUser.getRole() == UserRole.VIEWER
        ) {
            return;
        }

        /*
         * Branch-owned document.
         */
        if (document.getBranchId() != null) {
            branchService.getAccessibleBranchById(
                    document.getBranchId()
            );
            return;
        }

        /*
         * Member-owned document.
         */
        if (document.getMemberId() != null) {
            Member member =
                    memberRepository
                            .findById(document.getMemberId())
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Member not found with ID: "
                                                    + document.getMemberId()
                                    )
                            );

            if (member.getBranchId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Document member does not belong to a branch"
                );
            }

            branchService.getAccessibleBranchById(
                    member.getBranchId()
            );

            return;
        }

        /*
         * Activity-owned document.
         */
        if (document.getActivityId() != null) {
            var activity =
                    activityRepository
                            .findById(document.getActivityId())
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Activity not found with ID: "
                                                    + document.getActivityId()
                                    )
                            );

            if (activity.getBranchId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Document activity does not belong to a branch"
                );
            }

            branchService.getAccessibleBranchById(
                    activity.getBranchId()
            );

            return;
        }

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Document does not have a valid owner"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDocumentPageResponse getMemberDocuments(
            int page,
            int size,
            String search,
            Short typeId,
            Long branchId,
            LocalDate date
    ) {
        /*
         * Pagination validation.
         */
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must not be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Size must be between 1 and 100"
            );
        }

        /*
         * Normalize search.
         */
        String normalizedSearch =
                search == null
                        ? ""
                        : search.trim();

        /*
         * Validate document type when supplied.
         */
        if (
                typeId != null
                        && !documentTypeRepository.existsById(typeId)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Document type not found with ID: "
                            + typeId
            );
        }

        /*
         * Resolve logged-in user.
         */
        User currentUser =
                SecurityUtil.getCurrentUser();

        if (
                currentUser == null
                        || currentUser.getId() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        UserRole role =
                currentUser.getRole();

        if (role == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user does not have a role"
            );
        }

        /*
         * VIEWER has the same read authority as ADMIN throughout this
         * app (see UserRole's doc comment) — folded into isAdmin here.
         * isStaff deliberately does NOT include VIEWER, so the
         * branchService.getAccessibleBranchIds() call a few lines down
         * (which only knows about ADMIN/SECRETARY/BRANCH_LEADER and
         * would throw FORBIDDEN for VIEWER) is never reached for it.
         */
        boolean isAdmin =
                role == UserRole.ADMIN
                        || role == UserRole.VIEWER;

        boolean isMember =
                role == UserRole.MEMBER;

        boolean isStaff =
                role == UserRole.SECRETARY
                        || role == UserRole.BRANCH_LEADER;

        if (
                !isAdmin
                        && !isMember
                        && !isStaff
        ) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to view member documents"
            );
        }

        /*
         * MEMBER:
         * backend determines member ID.
         *
         * Frontend cannot select another member.
         */
        Long currentMemberId = null;

        if (isMember) {
            currentMemberId =
                    currentUser.getMemberId();

            if (currentMemberId == null) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Your account is not linked to a member record"
                );
            }

            /*
             * Member only sees own documents.
             * Ignore branch filter.
             */
            branchId = null;
        }

        /*
         * Branch scope for Secretary / Branch Leader.
         */
        Set<Long> accessibleBranchIds =
                Set.of(-1L);

        if (isStaff) {
            accessibleBranchIds =
                    branchService
                            .getAccessibleBranchIds();

            if (accessibleBranchIds.isEmpty()) {
                return new MemberDocumentPageResponse(
                        List.of(),
                        page,
                        size,
                        0,
                        0,
                        true,
                        true
                );
            }

            if (
                    branchId != null
                            && !accessibleBranchIds.contains(branchId)
            ) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "The selected branch is outside your accessible scope"
                );
            }
        }

        /*
         * ADMIN may filter by any valid branch.
         */
        if (
                isAdmin
                        && branchId != null
        ) {
            Long requestedBranchId =
                    branchId;

            branchRepository
                    .findById(requestedBranchId)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Branch not found with ID: "
                                            + requestedBranchId
                            )
                    );
        }

        /*
         * Date filter using Cambodia UTC+7.
         */
        ZoneOffset cambodiaOffset =
                ZoneOffset.ofHours(7);

        OffsetDateTime startDateTime =
                LocalDate
                        .of(1900, 1, 1)
                        .atStartOfDay()
                        .atOffset(cambodiaOffset);

        OffsetDateTime endDateTime =
                LocalDate
                        .of(9999, 1, 1)
                        .atStartOfDay()
                        .atOffset(cambodiaOffset);

        if (date != null) {
            startDateTime =
                    date
                            .atStartOfDay()
                            .atOffset(cambodiaOffset);

            endDateTime =
                    date
                            .plusDays(1)
                            .atStartOfDay()
                            .atOffset(cambodiaOffset);
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        ).and(
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "id"
                                )
                        )
                );

        Page<Document> result =
                documentRepository
                        .findMemberDocumentPage(
                                normalizedSearch,
                                typeId,
                                branchId,
                                startDateTime,
                                endDateTime,
                                isAdmin,
                                isMember,
                                isStaff,
                                currentMemberId,
                                accessibleBranchIds,
                                pageable
                        );

        /*
         * Build table response.
         */
        List<MemberDocumentTableItemResponse> content =
                result
                        .getContent()
                        .stream()
                        .map(document -> {

                            Member member =
                                    memberRepository
                                            .findById(
                                                    document.getMemberId()
                                            )
                                            .orElseThrow(() ->
                                                    new ResponseStatusException(
                                                            HttpStatus.NOT_FOUND,
                                                            "Member not found with ID: "
                                                                    + document.getMemberId()
                                                    )
                                            );

                            DocumentType documentType =
                                    documentTypeRepository
                                            .findById(
                                                    document.getTypeId()
                                            )
                                            .orElseThrow(() ->
                                                    new ResponseStatusException(
                                                            HttpStatus.NOT_FOUND,
                                                            "Document type not found with ID: "
                                                                    + document.getTypeId()
                                                    )
                                            );

                            return new MemberDocumentTableItemResponse(
                                    document.getId(),

                                    member.getId(),

                                    member.getFullNameKm(),

                                    member.getFullNameEn(),

                                    member.getGender() == null
                                            ? null
                                            : member.getGender().name(),

                                    documentType.getId(),

                                    documentType.getLabelKm(),

                                    documentType.getLabelEn(),

                                    document.getTitle(),

                                    document.getDescription(),

                                    document.getFileId(),

                                    document.getCreatedAt(),

                                    document.getUpdatedAt()
                            );
                        })
                        .toList();

        return new MemberDocumentPageResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentPageResponse getCrossBranchCertificateDocuments(
            int page,
            int size,
            String search,
            LocalDate date
    ) {
        validateCertificatePageParams(page, size);

        String normalizedSearch =
                search == null ? "" : search.trim();

        Set<Long> accessibleBranchIds =
                resolveAccessibleBranchIdsForCertificateView();

        if (accessibleBranchIds.isEmpty()) {
            return new DocumentPageResponse(
                    List.of(), page, size, 0, 0, true, true
            );
        }

        CertificateDateRange range =
                resolveCertificateDateRange(date);

        /*
         * No Sort here on purpose -- this is a native query with its own
         * hard-coded ORDER BY (using real column names). Spring Data
         * appends a Pageable's Sort as a second, literal "order by"
         * clause for native queries without translating property names
         * to columns, so a Sort.by("createdAt") here would append
         * "order by d.createdat" and blow up with a missing-column error.
         */
        Pageable pageable = PageRequest.of(page, size);

        Page<Document> result =
                documentRepository.findCrossBranchCertificateDocumentPage(
                        normalizedSearch,
                        range.startDateTime(),
                        range.endDateTime(),
                        accessibleBranchIds,
                        pageable
                );

        return toDocumentPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentPageResponse getCertificatesReceivedFromOtherBranches(
            int page,
            int size,
            String search,
            LocalDate date
    ) {
        validateCertificatePageParams(page, size);

        String normalizedSearch =
                search == null ? "" : search.trim();

        Set<Long> accessibleBranchIds =
                resolveAccessibleBranchIdsForCertificateView();

        if (accessibleBranchIds.isEmpty()) {
            return new DocumentPageResponse(
                    List.of(), page, size, 0, 0, true, true
            );
        }

        CertificateDateRange range =
                resolveCertificateDateRange(date);

        Pageable pageable = PageRequest.of(page, size);

        Page<Document> result =
                documentRepository.findCertificatesReceivedFromOtherBranchesPage(
                        normalizedSearch,
                        range.startDateTime(),
                        range.endDateTime(),
                        accessibleBranchIds,
                        pageable
                );

        return toDocumentPageResponse(result);
    }

    private void validateCertificatePageParams(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must not be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Size must be between 1 and 100"
            );
        }
    }

    /**
     * Shared role gate for both the cross-branch-issued and
     * received-from-other-branches certificate views: staff/admin/viewer
     * only. Admin/viewer manage every branch, so neither view has any
     * meaning for them -- nothing would ever be excluded either way,
     * which would just reproduce the main member-documents list. An
     * empty set (rather than every branch) signals the caller to return
     * an empty page instead.
     */
    private Set<Long> resolveAccessibleBranchIdsForCertificateView() {
        User currentUser =
                SecurityUtil.getCurrentUser();

        if (currentUser == null || currentUser.getRole() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        UserRole role = currentUser.getRole();

        boolean isAdmin =
                role == UserRole.ADMIN || role == UserRole.VIEWER;

        boolean isStaff =
                role == UserRole.SECRETARY || role == UserRole.BRANCH_LEADER;

        if (!isAdmin && !isStaff) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to view these certificates"
            );
        }

        if (isAdmin) {
            return Set.of();
        }

        return branchService.getAccessibleBranchIds();
    }

    private record CertificateDateRange(
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime
    ) {
    }

    private CertificateDateRange resolveCertificateDateRange(LocalDate date) {
        ZoneOffset cambodiaOffset = ZoneOffset.ofHours(7);

        OffsetDateTime startDateTime =
                LocalDate.of(1900, 1, 1)
                        .atStartOfDay()
                        .atOffset(cambodiaOffset);

        OffsetDateTime endDateTime =
                LocalDate.of(9999, 1, 1)
                        .atStartOfDay()
                        .atOffset(cambodiaOffset);

        if (date != null) {
            startDateTime = date.atStartOfDay().atOffset(cambodiaOffset);
            endDateTime = date.plusDays(1).atStartOfDay().atOffset(cambodiaOffset);
        }

        return new CertificateDateRange(startDateTime, endDateTime);
    }

    private DocumentPageResponse toDocumentPageResponse(Page<Document> result) {
        List<DocumentResponse> content =
                result.getContent()
                        .stream()
                        .map(documentMapper::toResponse)
                        .toList();

        return new DocumentPageResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }
}
