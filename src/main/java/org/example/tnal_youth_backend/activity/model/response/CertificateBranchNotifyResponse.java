package org.example.tnal_youth_backend.activity.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Result of {@code POST .../invited-branches/certificates/notify} — see
 * {@link org.example.tnal_youth_backend.activity.service.ActivityInvitedBranchService#notifyBranchesCertificatesReady}.
 * A branch lands in {@code skippedBranchIds} (never {@code notifiedBranchIds})
 * when it currently has no active SECRETARY/BRANCH_LEADER user account to
 * notify, or the notification itself could not be created -- reported back
 * rather than silently dropped so the caller can see it.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateBranchNotifyResponse {

    @JsonProperty("notified_branch_ids")
    private List<Long> notifiedBranchIds;

    @JsonProperty("skipped_branch_ids")
    private List<Long> skippedBranchIds;
}
