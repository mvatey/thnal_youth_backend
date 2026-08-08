package org.example.tnal_youth_backend.member.branch.dto.projection;

import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.member.member.entity.Member;

public interface BranchManagementProjection {

    Member getMember();

    UserRole getRole();
}