package org.example.tnal_youth_backend.activity.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityListItemResponse {

    private Long id;

    private String titleKm;

    private String titleEn;

    private ActivityResponse.LookupResponse type;

    private ActivityResponse.LookupResponse sector;

    private ActivityResponse.LookupResponse status;

    private Long branchId;

    private Boolean publicActivity;

    private OffsetDateTime startsAt;

    private OffsetDateTime endsAt;

    private String locationName;

    private Integer capacity;
}