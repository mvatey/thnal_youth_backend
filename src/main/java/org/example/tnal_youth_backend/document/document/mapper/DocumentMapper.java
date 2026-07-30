package org.example.tnal_youth_backend.document.document.mapper;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.document.document.dto.request.InstitutionalDocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.request.MemberDocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentDetailResponse;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentListItemResponse;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentPageResponse;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DocumentMapper {

    private final EntityManager entityManager;

    // =========================================================
    // INSTITUTIONAL CREATE / UPDATE
    // Existing logic unchanged
    // =========================================================

    /**
     * Converts an Institutional Document create request into a branch-owned
     * document.
     */
    public Document toInstitutionalEntity(
            Short typeId,
            Long fileId,
            Long uploadedById,
            InstitutionalDocumentRequest.Create request
    ) {
        if (request == null) {
            return null;
        }

        Document document = Document.builder()
                .typeId(typeId)
                .fileId(fileId)
                .uploadedById(uploadedById)
                .title(request.normalizedTitle())
                .description(request.normalizedDescription())
                .documentDate(LocalDate.now())
                .build();

        /*
         * Existing Institutional behavior remains unchanged.
         */
        document.assignBranchOwner(
                request.branchId()
        );

        return document;
    }

    /**
     * Updates an existing branch-owned Institutional Document.
     */
    public void updateInstitutionalEntity(
            Document document,
            Short typeId,
            Long fileId,
            InstitutionalDocumentRequest.Update request
    ) {
        if (document == null || request == null) {
            return;
        }

        document.setTypeId(typeId);
        document.setFileId(fileId);
        document.setTitle(request.normalizedTitle());
        document.setDescription(request.normalizedDescription());

        if (request.documentDate() != null) {
            document.setDocumentDate(
                    request.documentDate()
            );
        }

        /*
         * Existing Institutional behavior remains unchanged.
         */
        document.assignBranchOwner(
                request.branchId()
        );
    }

    // =========================================================
    // MEMBER DOCUMENT CREATE
    // =========================================================

    /**
     * Converts a Member Document request into a member-owned document.
     *
     * Ownership:
     *
     * member_id   = selected member
     * branch_id   = null
     * activity_id = null
     */
    public Document toMemberEntity(
            Short typeId,
            Long fileId,
            Long uploadedById,
            MemberDocumentRequest.Create request
    ) {
        if (request == null) {
            return null;
        }

        Document document = Document.builder()
                .typeId(typeId)
                .fileId(fileId)
                .uploadedById(uploadedById)
                .title(request.normalizedTitle())
                .description(request.normalizedDescription())
                .documentDate(request.effectiveDocumentDate())
                .build();

        /*
         * The branch selected in the frontend is used only to filter members.
         * It is not saved into documents.branch_id.
         */
        document.assignMemberOwner(
                request.memberId()
        );

        return document;
    }

    // =========================================================
    // RESPONSE MAPPING
    // =========================================================

    public DocumentListItemResponse toListItemResponse(
            Document document
    ) {
        if (document == null) {
            return null;
        }

        /*
         * These are the document's own Institutional branch fields.
         */
        Long branchId =
                document.getBranchId();

        String branchName =
                readBranchName(
                        document.getBranch()
                );

        /*
         * These fields describe the member owner.
         */
        Member member =
                document.getMember();

        Long memberId =
                resolveMemberId(document);

        String memberName =
                memberId == null
                        ? null
                        : readMemberName(member);

        String genderCode =
                memberId == null
                        ? null
                        : readGenderCode(member);

        String genderName =
                memberId == null
                        ? null
                        : readGenderName(member);

        Long memberBranchId =
                memberId == null || member == null
                        ? null
                        : member.getBranchId();

        String memberBranchName =
                memberId == null
                        ? null
                        : readMemberBranchName(
                        memberBranchId
                );

        return new DocumentListItemResponse(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                readTypeCode(document),
                readTypeName(document),
                document.getFileId(),
                readOriginalFileName(document),
                readMimeType(document),
                readFileExtension(document),
                readFileSizeBytes(document),
                readFileSizeMb(document),
                buildContentUrl(document.getFileId()),
                resolveOwnerType(document),
                resolveOwnerId(document),
                resolveOwnerName(document),

                /*
                 * Existing Institutional branch fields.
                 */
                branchId,
                branchName,

                /*
                 * Member-only fields.
                 */
                memberId,
                memberName,
                genderCode,
                genderName,
                memberBranchId,
                memberBranchName,

                effectiveDocumentDate(document)
        );
    }

    public DocumentDetailResponse toDetailResponse(
            Document document
    ) {
        if (document == null) {
            return null;
        }

        /*
         * Document-level Institutional branch.
         */
        Long branchId =
                document.getBranchId();

        String branchName =
                readBranchName(
                        document.getBranch()
                );

        /*
         * Member-owner information.
         */
        Member member =
                document.getMember();

        Long memberId =
                resolveMemberId(document);

        String memberName =
                memberId == null
                        ? null
                        : readMemberName(member);

        String genderCode =
                memberId == null
                        ? null
                        : readGenderCode(member);

        String genderName =
                memberId == null
                        ? null
                        : readGenderName(member);

        Long memberBranchId =
                memberId == null || member == null
                        ? null
                        : member.getBranchId();

        String memberBranchName =
                memberId == null
                        ? null
                        : readMemberBranchName(
                        memberBranchId
                );

        return new DocumentDetailResponse(
                document.getId(),
                document.getTypeId(),
                document.getTitle(),
                document.getDescription(),
                readTypeCode(document),
                readTypeName(document),
                document.getFileId(),
                readOriginalFileName(document),
                readMimeType(document),
                readFileSizeBytes(document),
                readFileSizeMb(document),
                buildContentUrl(document.getFileId()),

                /*
                 * Existing ownership-related values.
                 */
                document.getActivityId(),
                branchId,
                branchName,
                effectiveDocumentDate(document),
                resolveOwnerType(document),
                resolveOwnerId(document),
                resolveOwnerName(document),

                /*
                 * Member-only values.
                 */
                memberId,
                memberName,
                genderCode,
                genderName,
                memberBranchId,
                memberBranchName,

                /*
                 * Existing upload information.
                 */
                document.getUploadedById(),
                readUploaderName(
                        document.getUploadedBy()
                ),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    public DocumentPageResponse toPageResponse(
            Page<Document> documents
    ) {
        if (documents == null) {
            return DocumentPageResponse.empty(
                    0,
                    10
            );
        }

        return DocumentPageResponse.from(
                documents.map(
                        this::toListItemResponse
                )
        );
    }

    // =========================================================
    // DOCUMENT DATE
    // =========================================================

    private LocalDate effectiveDocumentDate(
            Document document
    ) {
        if (document.getDocumentDate() != null) {
            return document.getDocumentDate();
        }

        if (document.getCreatedAt() == null) {
            return null;
        }

        return document
                .getCreatedAt()
                .toLocalDate();
    }

    // =========================================================
    // FILE RESPONSE
    // =========================================================

    private String readOriginalFileName(
            Document document
    ) {
        if (document.getFile() == null) {
            return null;
        }

        return document
                .getFile()
                .getOriginalName();
    }

    private String readMimeType(
            Document document
    ) {
        if (document.getFile() == null) {
            return null;
        }

        return document
                .getFile()
                .getMimeType();
    }

    private Long readFileSizeBytes(
            Document document
    ) {
        if (document.getFile() == null) {
            return null;
        }

        return document
                .getFile()
                .getSizeBytes();
    }

    private Double readFileSizeMb(
            Document document
    ) {
        Long bytes =
                readFileSizeBytes(document);

        if (bytes == null) {
            return null;
        }

        double megabytes =
                bytes / (1024.0 * 1024.0);

        return Math.round(
                megabytes * 100.0
        ) / 100.0;
    }

    private String readFileExtension(
            Document document
    ) {
        String filename =
                readOriginalFileName(document);

        if (filename == null || filename.isBlank()) {
            return null;
        }

        int dotIndex =
                filename.lastIndexOf('.');

        if (
                dotIndex < 0
                        || dotIndex == filename.length() - 1
        ) {
            return null;
        }

        return filename
                .substring(dotIndex + 1)
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String buildContentUrl(
            Long fileId
    ) {
        if (fileId == null) {
            return null;
        }

        return "/api/files/"
                + fileId
                + "/content";
    }

    // =========================================================
    // DOCUMENT TYPE
    // =========================================================

    private String readTypeCode(
            Document document
    ) {
        if (document.getTypeCode() != null) {
            return document
                    .getTypeCode()
                    .name();
        }

        Object value =
                readValue(
                        document.getDocumentType(),
                        "getCode"
                );

        return value == null
                ? null
                : String.valueOf(value);
    }

    private String readTypeName(
            Document document
    ) {
        Object documentType =
                document.getDocumentType();

        return firstNonBlank(
                readString(
                        documentType,
                        "getLabelKm",
                        "getNameKm",
                        "getNameKh"
                ),
                readString(
                        documentType,
                        "getLabelEn",
                        "getNameEn",
                        "getName"
                )
        );
    }

    // =========================================================
    // OWNER RESPONSE
    // =========================================================

    private String resolveOwnerType(
            Document document
    ) {
        if (document == null) {
            return null;
        }

        if (document.getMemberId() != null) {
            return "MEMBER";
        }

        if (document.getBranchId() != null) {
            return "BRANCH";
        }

        if (document.getActivityId() != null) {
            return "ACTIVITY";
        }

        return "ORGANIZATION";
    }

    private Long resolveOwnerId(
            Document document
    ) {
        if (document == null) {
            return null;
        }

        if (document.getMemberId() != null) {
            return document.getMemberId();
        }

        if (document.getBranchId() != null) {
            return document.getBranchId();
        }

        if (document.getActivityId() != null) {
            return document.getActivityId();
        }

        return null;
    }

    private String resolveOwnerName(
            Document document
    ) {
        if (document == null) {
            return null;
        }

        if (document.getMemberId() != null) {
            String memberName =
                    readMemberName(
                            document.getMember()
                    );

            return memberName != null
                    ? memberName
                    : "Member #" + document.getMemberId();
        }

        if (document.getBranchId() != null) {
            String branchName =
                    readBranchName(
                            document.getBranch()
                    );

            return branchName != null
                    ? branchName
                    : "Branch #" + document.getBranchId();
        }

        if (document.getActivityId() != null) {
            String activityName =
                    readActivityName(
                            document.getActivity()
                    );

            return activityName != null
                    ? activityName
                    : "Activity #" + document.getActivityId();
        }

        return "អង្គភាព";
    }

    // =========================================================
    // MEMBER RESPONSE
    // =========================================================

    private Long resolveMemberId(
            Document document
    ) {
        if (
                document == null
                        || document.getMemberId() == null
        ) {
            return null;
        }

        return document.getMemberId();
    }

    private String readMemberName(
            Member member
    ) {
        if (member == null) {
            return null;
        }

        return firstNonBlank(
                member.getFullNameKm(),
                member.getFullNameEn()
        );
    }

    private String readGenderCode(
            Member member
    ) {
        if (
                member == null
                        || member.getGender() == null
        ) {
            return null;
        }

        return member
                .getGender()
                .name();
    }

    private String readGenderName(
            Member member
    ) {
        String genderCode =
                readGenderCode(member);

        if (genderCode == null) {
            return null;
        }

        return switch (
                genderCode.toUpperCase(Locale.ROOT)
                ) {
            case "MALE", "M" -> "ប្រុស";
            case "FEMALE", "F" -> "ស្រី";
            case "OTHER" -> "ផ្សេងៗ";
            default -> genderCode;
        };
    }

    /**
     * Resolves the selected member's branch from members.branch_id.
     *
     * This does not change documents.branch_id.
     */
    private String readMemberBranchName(
            Long memberBranchId
    ) {
        if (memberBranchId == null) {
            return null;
        }

        Branch branch =
                entityManager.find(
                        Branch.class,
                        memberBranchId
                );

        return readBranchName(branch);
    }

    // =========================================================
    // RELATED ENTITY NAMES
    // =========================================================

    private String readBranchName(
            Object branch
    ) {
        return firstNonBlank(
                readString(
                        branch,
                        "getNameKm",
                        "getBranchNameKm",
                        "getNameKh",
                        "getName"
                ),
                readString(
                        branch,
                        "getNameEn",
                        "getBranchNameEn"
                )
        );
    }

    private String readActivityName(
            Object activity
    ) {
        return firstNonBlank(
                readString(
                        activity,
                        "getTitleKm",
                        "getNameKm",
                        "getTitle",
                        "getName"
                ),
                readString(
                        activity,
                        "getTitleEn",
                        "getNameEn",
                        "getActivityName"
                )
        );
    }

    private String readUploaderName(
            Object uploader
    ) {
        return firstNonBlank(
                readString(
                        uploader,
                        "getFullNameKm",
                        "getNameKm",
                        "getFullName",
                        "getUsername"
                ),
                readString(
                        uploader,
                        "getFullNameEn",
                        "getNameEn",
                        "getEmail"
                )
        );
    }

    // =========================================================
    // REFLECTION HELPERS
    // =========================================================

    private String readString(
            Object source,
            String... methodNames
    ) {
        Object value =
                readValue(
                        source,
                        methodNames
                );

        if (value == null) {
            return null;
        }

        String text =
                String.valueOf(value)
                        .trim();

        return text.isEmpty()
                ? null
                : text;
    }

    private Object readValue(
            Object source,
            String... methodNames
    ) {
        if (source == null) {
            return null;
        }

        for (String methodName : methodNames) {
            try {
                Method method =
                        source
                                .getClass()
                                .getMethod(methodName);

                return method.invoke(source);

            } catch (ReflectiveOperationException ignored) {
                // Try the next supported getter.
            }
        }

        return null;
    }

    private String firstNonBlank(
            String first,
            String second
    ) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        if (second != null && !second.isBlank()) {
            return second;
        }

        return null;
    }
}