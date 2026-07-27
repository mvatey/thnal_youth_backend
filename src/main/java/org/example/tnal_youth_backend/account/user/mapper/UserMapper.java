package org.example.tnal_youth_backend.account.user.mapper;

import org.example.tnal_youth_backend.account.user.dto.response.UserResponse;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getId(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getFullNameKm(),
                user.getFullNameEn(),
                user.getProfileImage(),
                user.getLastLoginAt(),
                user.getFailedLoginCount(),
                user.getLockedUntil(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}