package org.example.tnal_youth_backend.activity.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

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

    private List<ActivityDailyScheduleRequest> dailySchedules;

    private Short provinceId;

    private Integer districtId;

    private Integer communeId;

    private String locationName;

    private String address;

    private String googleMapUrl;

    /**
     * Optional note explaining why the activity was cancelled -- only
     * meaningful when {@link #statusId} resolves to CANCELLED, but accepted
     * unconditionally here and left for the service layer to decide what
     * to do with it.
     */
    private String cancellationReason;

    @Positive(message = "Capacity must be greater than zero")
    private Integer capacity;

    private Long coverImageId;
}
