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