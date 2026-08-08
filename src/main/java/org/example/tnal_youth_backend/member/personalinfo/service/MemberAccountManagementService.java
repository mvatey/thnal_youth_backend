package org.example.tnal_youth_backend.member.personalinfo.service;

import org.example.tnal_youth_backend.authentication.model.enums.UserRole;

public interface MemberAccountManagementService {

    void updateRole(
            Long memberId,
            UserRole role
    );
}