package org.example.tnal_youth_backend.activity.expense.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActivityExpenseFamilyRequest {

    @NotBlank(message = "Family member name is required")
    @Size(max = 255, message = "Family member name must not exceed 255 characters")
    @JsonProperty("member_name")
    private String memberName;

    @Size(max = 2000, message = "Reason must not exceed 2000 characters")
    private String reason;
}
