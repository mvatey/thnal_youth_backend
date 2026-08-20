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

    private Long branchId;

    private String phone;

    private String email;

    private String fullNameKm;

    private String fullNameEn;

    private String profileImage;

    private String role;

    private String viewerScope;

    private String status;

    /*
     * For accounts linked to a member, these fields expose the
     * organizational Member status (members.status_id -> member_statuses).
     * The Users UI uses this same status as Member Detail and Member list.
     */
    private Short memberStatusId;

    private String memberStatusCode;

    private String memberStatusLabelKm;

    private String memberStatusLabelEn;

    private OffsetDateTime lastLoginAt;

    private OffsetDateTime createdAt;
}
