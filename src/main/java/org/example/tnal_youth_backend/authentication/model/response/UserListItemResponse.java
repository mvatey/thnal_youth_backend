package org.example.tnal_youth_backend.authentication.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListItemResponse {

    private Long id;

    private Long memberId;

    private String phone;

    private String email;

    private String fullNameKm;

    private String fullNameEn;

    private String profileImage;

    private String role;

    private String status;

    private OffsetDateTime lastLoginAt;

    private OffsetDateTime createdAt;
}
