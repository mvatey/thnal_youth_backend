package org.example.tnal_youth_backend.member.personalinfo.dto.response;

import org.example.tnal_youth_backend.authentication.model.enums.UserStatus;
import org.example.tnal_youth_backend.member.member.entity.Gender;

import java.time.LocalDate;

public record MemberPersonalInfoResponse(

        Long memberId,
        String fullNameKm,
        String fullNameEn,
        Gender gender,
        Short religionId,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String currentAddress,
        String permanentAddress,
        Long cvFileId,

        Long accountId,
        boolean hasAccount,
        UserStatus accountStatus

) {
}