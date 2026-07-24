package org.example.tnal_youth_backend.account.myaccount.dto.response;

import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.member.member.entity.Gender;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MyAccountResponse(

        /*
         * Account information from users
         */
        Long userId,

        Long memberId,

        UserRole role,

        UserStatus accountStatus,

        String phone,

        String email,

        OffsetDateTime lastLoginAt,

        /*
         * Member profile information from members
         */
        String memberNo,

        String fullNameKm,

        String fullNameEn,

        Gender gender,

        LocalDate dateOfBirth,

        String placeOfBirth,

        String currentAddress,

        String permanentAddress,

        LocalDate joinedOn,

        String bio,

        /*
         * Branch
         */
        Long branchId,

        /*
         * Member status
         */
        Short memberStatusId,

        String memberStatusCode,

        String memberStatusLabelKm,

        String memberStatusLabelEn,

        /*
         * Member level
         */
        Short memberLevelId,

        String memberLevelCode,

        String memberLevelLabelKm,

        String memberLevelLabelEn,

        /*
         * Religion
         */
        Short religionId,

        String religionCode,

        String religionLabelKm,

        String religionLabelEn,

        /*
         * Profile photo
         */
        Long profilePhotoId,

        String profilePhotoPath,

        String profilePhotoOriginalName,

        /*
         * Metadata
         */
        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {
}