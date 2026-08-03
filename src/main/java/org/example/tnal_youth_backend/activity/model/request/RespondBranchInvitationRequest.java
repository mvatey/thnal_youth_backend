package org.example.tnal_youth_backend.activity.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tnal_youth_backend.activity.model.enums.ActivityInvitationStatus;

@Getter
@Setter
@NoArgsConstructor
public class RespondBranchInvitationRequest {

    @NotNull(message = "Invitation status is required")
    @JsonProperty("invitation_status")
    private ActivityInvitationStatus invitationStatus;
}