package org.example.tnal_youth_backend.activity.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class UpdateActivityRequest {

    @NotBlank(message = "Khmer activity title is required")
    private String titleKm;

    private String titleEn;

    private String description;

    @NotNull(message = "Activity type is required")
    private Short typeId;

    @NotNull(message = "Activity sector is required")
    private Short sectorId;

    @NotNull(message = "Activity status is required")
    private Short statusId;

    @NotNull(message = "Branch is required")
    private Long branchId;

    private Boolean isPublic;

    @NotNull(message = "Activity start time is required")
    private OffsetDateTime startsAt;

    @NotNull(message = "Activity end time is required")
    private OffsetDateTime endsAt;

    private Short provinceId;

    private Integer districtId;

    private Integer communeId;

    private String locationName;

    private String address;

    private String googleMapUrl;

    @Positive(message = "Capacity must be greater than zero")
    private Integer capacity;

    private Long coverImageId;
}