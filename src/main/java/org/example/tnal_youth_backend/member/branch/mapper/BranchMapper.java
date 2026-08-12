package org.example.tnal_youth_backend.member.branch.mapper;

import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.member.branch.dto.response.*;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public BranchResponse toResponse(
            Branch branch
    ) {
        if (branch == null) {
            return null;
        }

        return new BranchResponse(
                branch.getId(),
                branch.getBranchCode(),
                branch.getNameKm(),
                branch.getNameEn(),
                branch.getBranchLevelId(),
                branch.getParentBranchId(),
                branch.getProvinceId(),
                branch.getDistrictId(),
                branch.getCommuneId(),
                branch.getStatusId(),
                branch.getAddress(),
                branch.getGoogleMapUrl(),
                branch.getPhone(),
                branch.getEmail(),
                branch.getCreatedById(),
                branch.getCreatedAt(),
                branch.getUpdatedAt()
        );
    }

    public BranchDetailResponse toDetailResponse(
            Branch branch
    ) {
        if (branch == null) {
            return null;
        }

        return new BranchDetailResponse(
                branch.getId(),
                branch.getBranchCode(),
                branch.getNameKm(),
                branch.getNameEn(),
                branch.getBranchLevelId(),
                branch.getParentBranchId(),
                branch.getProvinceId(),
                branch.getDistrictId(),
                branch.getCommuneId(),
                branch.getStatusId(),
                branch.getAddress(),
                branch.getGoogleMapUrl(),
                branch.getPhone(),
                branch.getEmail(),
                branch.getCreatedAt(),
                branch.getUpdatedAt()
        );
    }

    public BranchLeaderResponse toBranchLeaderResponse(
            Member member,
            UserRole role
    ) {
        if (member == null) {
            return null;
        }

        return new BranchLeaderResponse(
                member.getId(),

                member.getFullNameKm(),

                member.getFullNameEn(),

                member.getPhone(),

                member.getEmail(),

                member.getDateOfBirth(),

                member.getJoinedOn(),

                member.getGender(),

                role,

                member.getStatus() != null
                        ? member.getStatus().getCode()
                        : null,

                member.getProfilePhoto() != null
                        ? member.getProfilePhoto().getId()
                        : null,

                member.getProfilePhoto() != null
                        ? member.getProfilePhoto().getFilePath()
                        : null
        );
    }

    public BranchMemberTableItemResponse
    toBranchMemberTableItemResponse(
            Member member,
            UserRole role
    ) {
        if (member == null) {
            return null;
        }

        return new BranchMemberTableItemResponse(
                member.getId(),

                member.getFullNameKm(),

                member.getFullNameEn(),

                member.getPhone(),

                member.getEmail(),

                member.getGender(),

                role,

                member.getStatus() != null
                        ? member.getStatus().getId()
                        : null,

                member.getStatus() != null
                        ? member.getStatus().getCode()
                        : null,

                member.getStatus() != null
                        ? member.getStatus().getLabelKm()
                        : null,

                member.getStatus() != null
                        ? member.getStatus().getLabelEn()
                        : null,

                member.getLevel() != null
                        ? member.getLevel().getId()
                        : null,

                member.getJoinedOn(),

                member.getProfilePhoto() != null
                        ? member.getProfilePhoto().getId()
                        : null
        );
    }

    public BranchLeaderCandidateResponse
    toBranchLeaderCandidateResponse(
            Member member,
            UserRole role
    ) {
        if (member == null) {
            return null;
        }

        return new BranchLeaderCandidateResponse(
                member.getId(),

                member.getFullNameKm(),

                member.getFullNameEn(),

                member.getPhone(),

                member.getEmail(),

                member.getGender(),

                role,

                member.getProfilePhoto() != null
                        ? member.getProfilePhoto().getId()
                        : null
        );
    }
}