package org.example.tnal_youth_backend.activity.model.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class CreateActivityRequest {

    @NotBlank(message = "Khmer activity title is required")
    @Size(
            max = 255,
            message = "Khmer activity title must not exceed 255 characters"
    )
    private String titleKm;

    @Size(
            max = 255,
            message = "English activity title must not exceed 255 characters"
    )
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

    

    @NotNull(message = "Activity start time is required")
    @Future(message = "Activity start time must be in the future")
    private OffsetDateTime startsAt;

    @NotNull(message = "Activity end time is required")
    @Future(message = "Activity end time must be in the future")
    private OffsetDateTime endsAt;

    private Short provinceId;

    private Integer districtId;

    private Integer communeId;

    @Size(
            max = 255,
            message = "Location name must not exceed 255 characters"
    )
    private String locationName;

    private String address;

    private String googleMapUrl;

    @Positive(message = "Capacity must be greater than zero")
    private Integer capacity;

    private Long coverImageId;
}