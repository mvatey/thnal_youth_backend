package org.example.tnal_youth_backend.activity.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Body of {@code POST .../invited-branches/certificates/notify} — the
 * branches (other than the activity's own host branch) whose leadership
 * should be told their branch's certificates from this activity are ready.
 * See {@link org.example.tnal_youth_backend.activity.service.ActivityInvitedBranchService#notifyBranchesCertificatesReady}.
 */
@Getter
@Setter
@NoArgsConstructor
public class NotifyCertificateBranchesRequest {

    @NotEmpty(message = "At least one branch ID is required")
    @JsonProperty("branch_ids")
    private List<Long> branchIds;
}
