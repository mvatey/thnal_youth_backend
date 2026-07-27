package org.example.tnal_youth_backend.activity.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InviteParticipantsRequest {

    @NotEmpty(message = "At least one member ID is required")
    @Size(
            max = 200,
            message = "A maximum of 200 members may be added at once"
    )
    @JsonProperty("member_ids")
    private List<Long> memberIds;

    @Size(
            max = 1000,
            message = "Participant note must not exceed 1000 characters"
    )
    private String note;
}