package org.example.tnal_youth_backend.document.document.mapper;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentResponse;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentMapper {

    private final UserRepository userRepository;

    public DocumentResponse toResponse(
            Document document
    ) {

        if (document == null) {
            return null;
        }

        return new DocumentResponse(

                document.getId(),

                document.getTitle(),

                document.getDescription(),

                toDocumentType(document),

                toFile(document.getFile()),

                toBranch(document),

                toMember(document),

                toActivity(document),

                toUploadedBy(document.getUploadedById()),

                ownerType(document),

                ownerId(document),

                document.getCreatedAt(),

                document.getUpdatedAt()
        );
    }

    private DocumentResponse.DocumentTypeResponse toDocumentType(
            Document document
    ) {

        if (document.getDocumentType() == null) {
            return null;
        }

        return new DocumentResponse.DocumentTypeResponse(

                document.getDocumentType().getId(),

                document.getDocumentType().getCode(),

                document.getDocumentType().getLabelKm(),

                document.getDocumentType().getLabelEn()
        );
    }

    private DocumentResponse.FileResponse toFile(
            FileEntity file
    ) {

        if (file == null) {
            return null;
        }

        double sizeKb =
                file.getSizeBytes() == null
                        ? 0
                        : file.getSizeBytes() / 1024.0;

        double sizeMb =
                file.getSizeBytes() == null
                        ? 0
                        : file.getSizeBytes() / (1024.0 * 1024.0);

        return new DocumentResponse.FileResponse(

                file.getId(),

                file.getFilePath(),

                "/api/files/" + file.getId() + "/content",

                file.getOriginalName(),

                file.getMimeType(),

                file.getSizeBytes(),

                Math.round(sizeKb * 100.0) / 100.0,

                Math.round(sizeMb * 100.0) / 100.0
        );
    }

    private String ownerType(Document document) {
        if (document.getBranchId() != null) {
            return "BRANCH";
        }
        if (document.getMemberId() != null) {
            return "MEMBER";
        }
        return document.getActivityId() == null ? null : "ACTIVITY";
    }

    private Long ownerId(Document document) {
        if (document.getBranchId() != null) {
            return document.getBranchId();
        }
        if (document.getMemberId() != null) {
            return document.getMemberId();
        }
        return document.getActivityId();
    }

    private DocumentResponse.BranchResponse toBranch(
            Document document
    ) {

        if (document.getBranch() == null) {
            return null;
        }

        return new DocumentResponse.BranchResponse(

                document.getBranch().getId(),

                document.getBranch().getNameKm(),

                document.getBranch().getNameEn()
        );
    }

    private DocumentResponse.MemberResponse toMember(
            Document document
    ) {

        if (document.getMember() == null) {
            return null;
        }

        return new DocumentResponse.MemberResponse(

                document.getMember().getId(),

                document.getMember().getMemberNo(),

                document.getMember().getFullNameKm(),

                document.getMember().getFullNameEn()
        );
    }

    private DocumentResponse.ActivityResponse toActivity(
            Document document
    ) {

        if (document.getActivity() == null) {
            return null;
        }

        return new DocumentResponse.ActivityResponse(

                document.getActivity().getId(),

                document.getActivity().getTitleKm(),

                document.getActivity().getTitleEn()
        );
    }

    private DocumentResponse.UploadedByResponse toUploadedBy(
            Long uploadedById
    ) {

        if (uploadedById == null) {
            return null;
        }

        User user = userRepository
                .findById(uploadedById)
                .orElse(null);

        if (user == null) {
            return null;
        }

        return new DocumentResponse.UploadedByResponse(

                user.getId(),

                user.getFullNameKm(),

                user.getFullNameEn()
        );
    }
}
