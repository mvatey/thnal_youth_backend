package org.example.tnal_youth_backend.document.document.mapper;

import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.document.document.dto.response.DocumentResponse;
import org.example.tnal_youth_backend.document.document.entity.Document;
import org.example.tnal_youth_backend.file.entity.FileEntity;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentMapper {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

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

                file.getOriginalName(),

                file.getMimeType(),

                file.getSizeBytes(),

                Math.round(sizeKb * 100.0) / 100.0,

                Math.round(sizeMb * 100.0) / 100.0
        );
    }

    private DocumentResponse.BranchResponse toBranch(
            Document document
    ) {

        Branch branch = document.getBranch();

        if (branch == null
                && document.getMember() != null
                && document.getMember().getBranchId() != null) {
            branch = branchRepository
                    .findById(document.getMember().getBranchId())
                    .orElse(null);
        }

        if (branch == null) {
            return null;
        }

        return new DocumentResponse.BranchResponse(

                branch.getId(),

                branch.getNameKm(),

                branch.getNameEn()
        );
    }

    private DocumentResponse.MemberResponse toMember(
            Document document
    ) {

        if (document.getMember() == null) {
            return null;
        }

        Gender gender = document.getMember().getGender();

        return new DocumentResponse.MemberResponse(

                document.getMember().getId(),

                document.getMember().getMemberNo(),

                document.getMember().getFullNameKm(),

                document.getMember().getFullNameEn(),

                gender == null ? null : gender.name(),

                gender == null ? null : gender.getLabelKm(),

                gender == null ? null : gender.getLabelEn()
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
