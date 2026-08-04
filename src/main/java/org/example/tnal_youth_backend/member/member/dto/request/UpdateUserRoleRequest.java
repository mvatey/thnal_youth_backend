package org.example.tnal_youth_backend.member.member.dto.request;

import org.example.tnal_youth_backend.authentication.model.enums.UserRole;

public record UpdateUserRoleRequest(
        UserRole role
) {
}