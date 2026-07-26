package org.example.tnal_youth_backend.account.myaccount.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MyAccountResponse(

        Long userId,

        Long memberId,

        String memberNo,

        UserRole role,

        String fullNameKm,

        String fullNameEn,

        GenderResponse gender,

        BranchResponse branch,

        LookupResponse level,

        LookupResponse status,

        String phone,

        String email,

        LocalDate dateOfBirth,

        LocalDate joinedOn,

        ProfilePhotoResponse profilePhoto,

        CvFileResponse cvFile
) {

    public record GenderResponse(
            String code,
            String labelKm,
            String labelEn
    ) {
    }

    public record BranchResponse(
            Long id,
            String nameKm
    ) {
    }

    public record LookupResponse(
            Short id,
            String code,
            String labelKm,
            String labelEn
    ) {
    }

    public record ProfilePhotoResponse(
            Long id,
            String url
    ) {
    }

    public record CvFileResponse(
            Long id,
            String url,
            String originalName,
            String mimeType,
            Long sizeBytes
    ) {
    }
}