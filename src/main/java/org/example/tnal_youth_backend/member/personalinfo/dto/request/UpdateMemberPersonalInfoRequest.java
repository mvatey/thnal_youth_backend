package org.example.tnal_youth_backend.member.personalinfo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.tnal_youth_backend.member.member.entity.Gender;

import java.time.LocalDate;

public record UpdateMemberPersonalInfoRequest(

        @NotBlank(message = "Khmer name is required")
        @Size(max = 255, message = "Khmer name must not exceed 255 characters")
        String fullNameKm,

        @Size(max = 255, message = "English name must not exceed 255 characters")
        String fullNameEn,

        Gender gender,

        Short religionId,

        @Email(message = "Email format is invalid")
        String email,

        @Size(max = 30, message = "Phone number must not exceed 30 characters")
        String phone,

        LocalDate dateOfBirth,

        String currentAddress,

        String permanentAddress,

        Long cvFileId

) {
}