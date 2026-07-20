package org.example.tnal_youth_backend.authentication.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;

    private String phone;

    private String email;

    private String fullNameKm;

    private String fullNameEn;

    private String profileImage;

    private String role;
}