package org.example.tnal_youth_backend.activity.model.response;

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
public class ActivityResponse {

    private Long id;

    private String titleKm;

    private String titleEn;

    private String description;

    private LookupResponse type;

    private LookupResponse sector;

    private LookupResponse status;

    private Long branchId;

    private Boolean publicActivity;

    private OffsetDateTime startsAt;

    private OffsetDateTime endsAt;

    private Short provinceId;

    private Integer districtId;

    private Integer communeId;

    private String locationName;

    private String address;

    private String googleMapUrl;

    private Integer capacity;

    private Long coverImageId;

    private Long createdBy;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    /**
     * Whether the CURRENT caller (not the activity's creator) may edit this
     * activity's info and manually correct its attendance — true only for a
     * branch leader or secretary who is staff of this activity's own branch.
     * Computed per-request in the service layer, never persisted.
     */
    private Boolean canManage;

    /**
     * Whether the CURRENT caller is a branch leader or secretary of a branch
     * that has an ACCEPTED invitation to this activity (i.e. a co-hosting
     * branch, not the host itself). Such a caller may invite/remove members
     * of their OWN branch, and record income/expense for this activity, but
     * may never edit the activity's own info — that stays {@link #canManage}
     * only. Always {@code false} when {@link #canManage} is {@code true}.
     * Computed per-request in the service layer, never persisted.
     */
    private Boolean canManageAsInvitedBranch;

    /**
     * The id of the branch the current caller manages as an accepted invited
     * branch (see {@link #canManageAsInvitedBranch}), or {@code null}. Lets
     * the frontend scope the member-invite picker to just that branch.
     */
    private Long managedInvitedBranchId;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LookupResponse {

        private Short id;

        private String code;

        private String labelKm;

        private String labelEn;
    }
}