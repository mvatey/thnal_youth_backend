package org.example.tnal_youth_backend.account.myaccount.mapper;

import org.example.tnal_youth_backend.account.myaccount.dto.response.MyAccountResponse;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class MyAccountMapper {

    public MyAccountResponse toResponse(User user) {

        if (user == null) {
            return null;
        }

        return new MyAccountResponse(
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