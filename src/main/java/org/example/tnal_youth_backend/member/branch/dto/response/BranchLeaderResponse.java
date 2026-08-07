package org.example.tnal_youth_backend.member.branch.dto.response;

import java.time.LocalDate;

public record BranchLeaderResponse(
        Long id,
        String nameKm,
        String nameEn,
        String gender,
        String status,
        String phone,
        String email,
        LocalDate dateOfBirth,
        LocalDate joinedAt,
        Long profilePhotoId,
        String profileImage,
        String role
) {}
