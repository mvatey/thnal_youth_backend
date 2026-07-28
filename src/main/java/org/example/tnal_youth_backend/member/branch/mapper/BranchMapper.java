package org.example.tnal_youth_backend.member.branch.mapper;

import org.example.tnal_youth_backend.member.branch.dto.response.BranchResponse;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public BranchResponse toResponse(Branch branch) {
        if (branch == null) {
            return null;
        }

        return new BranchResponse(
                branch.getId(),
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
}