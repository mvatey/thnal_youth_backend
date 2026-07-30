package org.example.tnal_youth_backend.document.document.mapper;

import org.example.tnal_youth_backend.document.document.dto.request.InstitutionalDocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.request.MemberDocumentRequest;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentDetailResponse;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentListItemResponse;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentPageResponse;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@SuppressWarnings("SpellCheckingInspection")
public class DocumentMapper {

    public Document toInstitutionalEntity(
            InstitutionalDocumentRequest request,
            Long uploadedById
    ) {
        if (request == null) {
            return null;
        }

        Document document = Document.builder()
                .typeId(request.typeId())
                .fileId(request.fileId())
                .title(request.normalizedTitle())
                .description(request.normalizedDescription())
                .uploadedById(uploadedById)
                .build();

        assignInstitutionalOwner(
                document,
                request
        );

        return document;
    }

    public void updateInstitutionalEntity(
            Document document,
            InstitutionalDocumentRequest request
    ) {
        if (document == null || request == null) {
            return;
        }

        document.setTypeId(request.typeId());
        document.setFileId(request.fileId());
        document.setTitle(request.normalizedTitle());
        document.setDescription(request.normalizedDescription());

        assignInstitutionalOwner(
                document,
                request
        );
    }

    public Document toMemberEntity(
            Long memberId,
            MemberDocumentRequest request,
            Long uploadedById
    ) {
        if (request == null) {
            return null;
        }

        Document document = Document.builder()
                .typeId(request.typeId())
                .fileId(request.fileId())
                .title(request.normalizedTitle())
                .description(request.normalizedDescription())
                .uploadedById(uploadedById)
                .build();

        assignMemberOwner(
                document,
                memberId,
                request
        );

        return document;
    }

    public void updateMemberEntity(
            Document document,
            Long memberId,
            MemberDocumentRequest request
    ) {
        if (document == null || request == null) {
            return;
        }

        document.setTypeId(request.typeId());
        document.setFileId(request.fileId());
        document.setTitle(request.normalizedTitle());
        document.setDescription(request.normalizedDescription());

        assignMemberOwner(
                document,
                memberId,
                request
        );
    }

    public DocumentListItemResponse toListItemResponse(
            Document document
    ) {
        if (document == null) {
            return null;
        }

        return new DocumentListItemResponse(
                document.getId(),
                document.getTitle(),
                readTypeCode(document),
                readTypeName(document),
                document.getFileId(),
                resolveOwnerType(document),
                resolveOwnerId(document),
                resolveOwnerName(document),
                document.getCreatedAt() == null
                        ? null
                        : document.getCreatedAt().toLocalDate()
        );
    }

    public DocumentDetailResponse toDetailResponse(
            Document document
    ) {
        if (document == null) {
            return null;
        }

        return new DocumentDetailResponse(
                document.getId(),
                document.getTypeId(),
                document.getTitle(),
                document.getDescription(),
                readTypeCode(document),
                readTypeName(document),
                document.getFileId(),
                document.getActivityId(),
                resolveOwnerType(document),
                resolveOwnerId(document),
                resolveOwnerName(document),
                document.getUploadedById(),
                readUploaderName(document.getUploadedBy()),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    public Page<DocumentListItemResponse> toListItemPage(
            Page<Document> documents
    ) {
        if (documents == null) {
            return Page.empty();
        }

        return documents.map(this::toListItemResponse);
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

        Page<DocumentListItemResponse> responsePage =
                toListItemPage(documents);

        return DocumentPageResponse.from(responsePage);
    }

    private String readTypeCode(
            Document document
    ) {
        if (document == null) {
            return null;
        }

        Object value = readValue(
                document.getDocumentType(),
                "getCode",
                "getTypeCode"
        );

        String mappedCode = convertDisplayValue(value);

        if (mappedCode != null) {
            return mappedCode;
        }

        if (document.getTypeCode() != null) {
            return document.getTypeCode().name();
        }

        return null;
    }

    private String readTypeName(
            Document document
    ) {
        if (document == null) {
            return null;
        }

        Object documentType = document.getDocumentType();

        return firstNonBlank(
                readString(
                        documentType,
                        "getLabelKm",
                        "getNameKm",
                        "getLabelKh",
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
            return readMemberName(
                    document.getMember()
            );
        }

        if (document.getBranchId() != null) {
            return readBranchName(
                    document.getBranch()
            );
        }

        if (document.getActivityId() != null) {
            return readActivityTitle(
                    document.getActivity()
            );
        }

        return "អង្គភាព";
    }

    private String readMemberName(
            Object member
    ) {
        return firstNonBlank(
                readString(
                        member,
                        "getFullNameKm",
                        "getNameKm",
                        "getFullNameKh",
                        "getNameKh"
                ),
                readString(
                        member,
                        "getFullNameEn",
                        "getNameEn",
                        "getEnglishName"
                )
        );
    }

    private String readBranchName(
            Object branch
    ) {
        return firstNonBlank(
                readString(
                        branch,
                        "getNameKm",
                        "getBranchNameKm",
                        "getNameKh",
                        "getBranchNameKh"
                ),
                readString(
                        branch,
                        "getNameEn",
                        "getBranchNameEn",
                        "getEnglishName"
                )
        );
    }

    private String readActivityTitle(
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
                        "getFullNameKh",
                        "getNameKh",
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

    private void assignInstitutionalOwner(
            Document document,
            InstitutionalDocumentRequest request
    ) {
        if (document == null || request == null) {
            return;
        }

        if (request.hasBranchOwner()) {
            document.assignBranchOwner(
                    request.branchId()
            );
            return;
        }

        if (request.hasActivityOwner()) {
            document.assignActivityOwner(
                    request.activityId()
            );
            return;
        }

        document.assignOrganizationOwner();
    }

    private void assignMemberOwner(
            Document document,
            Long memberId,
            MemberDocumentRequest request
    ) {
        if (document == null || request == null) {
            return;
        }

        if (request.hasActivity()) {
            document.assignActivityCertificateOwner(
                    memberId,
                    request.activityId()
            );
            return;
        }

        document.assignMemberOwner(memberId);
    }

    private String readString(
            Object source,
            String... methodNames
    ) {
        Object value = readValue(
                source,
                methodNames
        );

        if (value == null) {
            return null;
        }

        String text = String.valueOf(value).trim();

        return text.isEmpty()
                ? null
                : text;
    }

    private String convertDisplayValue(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }

        String text = String.valueOf(value).trim();

        return text.isEmpty()
                ? null
                : text;
    }

    private Object readValue(
            Object source,
            String... methodNames
    ) {
        if (source == null
                || methodNames == null
                || methodNames.length == 0) {
            return null;
        }

        for (String methodName : methodNames) {
            try {
                Method method = source
                        .getClass()
                        .getMethod(methodName);

                return method.invoke(source);
            } catch (ReflectiveOperationException ignored) {
                // Continue to the next supported getter name.
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