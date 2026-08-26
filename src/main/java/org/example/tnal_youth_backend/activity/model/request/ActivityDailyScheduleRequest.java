package org.example.tnal_youth_backend.activity.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
public class ActivityDailyScheduleRequest {
    @NotNull private LocalDate scheduleDate;
    @NotNull private LocalTime startsAt;
    @NotNull private LocalTime endsAt;
}
