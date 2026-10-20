package org.example.tnal_youth_backend.activity.expense.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityExpenseFamilyResponse {

    private Long id;

    @JsonProperty("activity_id")
    private Long activityId;

    @JsonProperty("member_name")
    private String memberName;

    private String reason;
}
