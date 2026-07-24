package org.example.tnal_youth_backend.authentication.model.response;


import lombok.*;

import org.example.tnal_youth_backend.authentication.model.enums.UserRole;


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


    private UserRole role;
}