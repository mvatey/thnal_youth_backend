package org.example.tnal_youth_backend.authentication.model.response;



import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private Long userId;

    private String fullName;

    private UserRole role;

    private String accessToken;

    private String refreshToken;

}